package com.dd.ddaiagent.chatMemory;


import com.dd.ddaiagent.entity.ChatMemoryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatMemory implements ChatMemory {

    private final RedissonClient redissonClient;
    //定义redis存储对话记录的前缀
    private final String CHAT_MEMORY_PREFIX = "CHAT_MEMORY:";

    @Override
    public void add(String conversationId, Message message) {
        RList<ChatMemoryEntity> chatList = redissonClient.getList(CHAT_MEMORY_PREFIX + conversationId);
        chatList.add(new ChatMemoryEntity(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        RList<ChatMemoryEntity> chatList = redissonClient.getList(CHAT_MEMORY_PREFIX + conversationId);
        List<ChatMemoryEntity> chatMemoryEntityList = messages.stream().map(ChatMemoryEntity::new).toList();
        chatList.addAll(chatMemoryEntityList);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        RList<ChatMemoryEntity> chatList = redissonClient.getList(CHAT_MEMORY_PREFIX + conversationId);
        List<Message> messageList = new ArrayList<>();
        if (CollectionUtils.isEmpty(chatList) || lastN <= 0) {
            return messageList;
        }
        // 处理lastN参数
        List<ChatMemoryEntity> entities;
        if (lastN < chatList.size()) {
            // 如果lastN大于0且小于列表总长度，获取最后N条消息
            int startIndex = chatList.size() - lastN;
            entities = chatList.range(startIndex, chatList.size() - 1);
        } else {
            // 如果lastN不合理或者要求获取全部，则获取所有消息
            entities = chatList;
        }

        for (ChatMemoryEntity chatMemory : entities) {
            String type = chatMemory.getType();
            String text = chatMemory.getText();
            if (MessageType.USER.getValue().equals(type)) {
                messageList.add(new UserMessage(text));
            } else if (MessageType.SYSTEM.getValue().equals(type)) {
                messageList.add(new SystemMessage(text));
            } else if (MessageType.ASSISTANT.getValue().equals(type)) {
                messageList.add(new AssistantMessage(text));
            }
        }
        return messageList;
    }

    @Override
    public void clear(String conversationId) {
        redissonClient.getList(CHAT_MEMORY_PREFIX + conversationId).delete();
    }
}
