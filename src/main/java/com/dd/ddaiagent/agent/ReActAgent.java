package com.dd.ddaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct（Reasoning and Acting) 模式的代理抽象类
 * 实现思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {
    /**
     * 处理当前状态并决定下一步行动
     * @return 是否需要执行任务
     */
    public abstract boolean think();

    /**
     * 决定执行的任务
     * @return 执行的任务结果
     */
    public abstract String act();

    /**
     * 执行单个步骤： 思考和行动
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成, 无需行动";
            }
            return act();
        } catch (Exception e) {
            //记录异常日志
            e.printStackTrace();
            return "步骤执行失败： " + e.getMessage();
        }
    }
}
