package com.dd.ddaiagent.app;

import reactor.core.publisher.Flux;

/**
 * 用于管理多个APP之间的对话方法 方便后续应用扩展
 */
public interface AIAppStrategy {

    /**
     *  同步对话接口
     */
    String doChat(String userMsg, String chatId);

    /**
     * 流式对话接口
     */
    Flux<String> doChatByStream(String userMsg, String chatId);

    /**
     * 基于RAG知识库的对话接口
     */
    String doChatWithRag(String message, String chatId);
}
