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
public class ManusController {
    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


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
