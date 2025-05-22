package com.dd.ddaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.dd.ddaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

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
     * 执行单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {};
}
