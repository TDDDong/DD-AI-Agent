package com.dd.ddaiagent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.io.Serializable;

/**
 * 自定义对话记录存储
 * redis持久化对象需要可序列化 故需将Message转为该对象进行持久化存储
 */
@Data
@NoArgsConstructor
public class ChatMemoryEntity implements Serializable {

    private String type;

    private String text;

    public ChatMemoryEntity(Message message) {
        this.type = message.getMessageType().getValue();
        this.text = message.getText();
    }
}
