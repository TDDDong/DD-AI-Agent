package com.dd.ddaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.dd.ddaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类 用于管理代理状态和执行流程
 *
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现step()抽象方法
 */
@Data
@Slf4j
public abstract class BaseAgent {

    //核心属性
    private String name;
    //系统提示
    private String systemPrompt;

    private String nextStepPrompt;
    //状态 默认是空闲
    private AgentState state = AgentState.IDLE;
    //执行步骤控制
    private int currentStep = 0;

    private int maxSteps = 10;
    //LLM
    private ChatClient chatClient;
    //需要自主维护会话上下文
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理 (模板方法）
     * @param userPrompt
     * @return 运行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        //更改运行状态
        state = AgentState.RUNNING;
        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("Executing step " + currentStep + "/" + maxSteps);
                //单步执行
                String stepResult = step();
                String result = "Step " + currentStep + ": " +stepResult;
                results.add(result);
            }
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join(",", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            //清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理 (流式输出）
     * @param userPrompt
     * @return 运行结果
     */
    public SseEmitter runStream(String userPrompt) {
        //创建一个带有超时时间的SseEmitter 智能体执行时间可设置长点
        SseEmitter emitter = new SseEmitter(300000L);
        //这里要用异步编程 智能体执行时间较长 避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理：" + this.state);
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }
                //更改运行状态
                state = AgentState.RUNNING;
                //记录消息上下文
                messageList.add(new UserMessage(userPrompt));

                try {
                    for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                        currentStep = i + 1;
                        log.info("Executing step " + currentStep + "/" + maxSteps);
                        //单步执行
                        String stepResult = step();
                        String result = "Step " + currentStep + ": " +stepResult;
                        //发送每一步的结果
                        emitter.send(result);
                    }
                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        emitter.send("执行结束：达到最大步骤 (" + maxSteps + ")");
                    }
                    //正常完成
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("Error executing agent", e);
                    try {
                        emitter.send("执行错误：" + e.getMessage());
                    } catch (IOException ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    //清理资源
                    this.cleanup();
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        //设置超时和完成回调
        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        return emitter;
    }

    /**
     * 执行单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {};
}
