package com.dd.ddaiagent.controller;

import cn.hutool.core.util.StrUtil;
import com.dd.ddaiagent.AppTypeEnum;
import com.dd.ddaiagent.app.AIAppStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AppController {

    @Autowired
    private Map<String, AIAppStrategy> appStrategyMap;

    /**
     * 同步输出的AI对话
     */
    @GetMapping("/{appType}/chat/sync")
    public String doChatWithLoveAppSync(@PathVariable String appType, String message, String chatId) {
        String appName = AppTypeEnum.findNameByType(appType);
        if (StrUtil.isBlank(appName)) {
            return "传输的appType有误";
        }
        return appStrategyMap.get(appName).doChat(message, chatId);
    }

    /**
     * 基于SSE流式输出的AI对话
     */
    @GetMapping(value = "/{appType}/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(@PathVariable String appType, String message, String chatId) {
        String appName = AppTypeEnum.findNameByType(appType);
        if (StrUtil.isBlank(appName)) {
            return Flux.just("传输的appType有误");
        }
        return appStrategyMap.get(appName).doChatByStream(message, chatId);
    }

    /**
     * 基于SseEmitter实现的AI流式对话
     */
    @GetMapping("/{appType}/chat/see/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(@PathVariable String appType, String message, String chatId) {
        //创建一个带有超时时间的SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L);
        String appName = AppTypeEnum.findNameByType(appType);
        if (StrUtil.isBlank(appName)) {
            sseEmitter.completeWithError(new RuntimeException("传输的appType有误"));
            return sseEmitter;
        }
        //获取并订阅Flux数据流
        appStrategyMap.get(appName).doChatByStream(message, chatId)
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
}
