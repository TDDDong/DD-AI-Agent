package com.dd.ddaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
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
        String content = loveApp.doChat("你好, 我的名字叫dd", chatId);
        System.out.println("第一轮对话：" + content);

        content = loveApp.doChat("我的名字是什么？", chatId);
        System.out.println("第二轮对话：" + content);
    }

    @Test
    void queryWeather() {
        String chatId = UUID.randomUUID().toString();
        String content = loveApp.doChat("帮我查询一下广州市的实时天气情况", chatId);
        System.out.println("查询结果：" + content);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是dd，我想让另一半更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMCP() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
        /*String message = "我的另一半居住在广东省广州市，请帮我找到 5 公里内合适的约会地点";
        String answer =  loveApp.doChatWithMCP(message, chatId);*/
        //测试mcp服务
        String message = "帮我搜一些能哄另一半开心的图片";
        String answer = loveApp.doChatWithMCP(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        testMessage("帮我转换广东省广州市海珠区的地理编码");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

}