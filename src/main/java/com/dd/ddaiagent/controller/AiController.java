package com.dd.ddaiagent.controller;

import com.dd.ddaiagent.agent.DDManus;
import com.dd.ddaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步输出的AI对话
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * 基于SSE流式输出的AI对话
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * 基于SseEmitter实现的AI流式对话
     */
    @GetMapping("/love_app/chat/see/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        //创建一个带有超时时间的SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L);
        //获取并订阅Flux数据流
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        //处理每条消息
                        chunk -> {
                            try {
                                sseEmitter.send(chunk);
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        //处理错误
                        sseEmitter::completeWithError,
                        //任务完成
                        sseEmitter::complete
                );
        //返回SseEmitter
        return sseEmitter;
    }

    /**
     * 流式调用 Manus智能体对话
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        //这里每次调用都需要创建一个新的实例 不能由Spring容器管理
        DDManus ddManus = new DDManus(allTools, dashscopeChatModel);
        return ddManus.runStream(message);
    }
}
