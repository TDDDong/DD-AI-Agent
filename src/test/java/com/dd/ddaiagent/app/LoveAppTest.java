package com.dd.ddaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        String content = loveApp.doChat("你好, 我是dd", chatId);
        System.out.println("第一轮对话：" + content);

        content = loveApp.doChat("我是谁？", chatId);
        System.out.println("第二轮对话：" + content);
    }
}