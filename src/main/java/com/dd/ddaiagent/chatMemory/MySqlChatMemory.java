package com.dd.ddaiagent.chatMemory;

import com.dd.ddaiagent.entity.ChatMemoryEntity;
import com.dd.ddaiagent.mapper.mysql.ChatMemoryMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MySqlChatMemory implements ChatMemory {

    @Resource
    private ChatMemoryMapper chatMemoryMapper;

    @Override
    public void add(String conversationId, Message message) {
        chatMemoryMapper.insert(conversationId, message.getMessageType().getValue(), message.getText());
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMemoryEntity> entityList = messages.stream()
                .map(msg -> new ChatMemoryEntity(conversationId, msg))
                .collect(Collectors.toList());
        chatMemoryMapper.batchInsert(entityList);
    }

    @Override
    public List<Message> get(String conversationId) {
        // 获取消息实体
        List<ChatMemoryEntity> entities;
        entities = chatMemoryMapper.findByConversationId(conversationId);

        // 将实体转换为消息对象
        List<Message> messageList = new ArrayList<>();
        if (entities.isEmpty()) {
            return messageList;
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
        chatMemoryMapper.deleteByConversationId(conversationId);
    }
}
