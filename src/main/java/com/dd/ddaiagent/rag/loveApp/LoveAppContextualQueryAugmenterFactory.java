package com.dd.ddaiagent.rag.loveApp;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class LoveAppContextualQueryAugmenterFactory {
    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以联系客服邮箱 dd126@email.com
                """);
        return ContextualQueryAugmenter.builder()
                /**
                 * if (this.allowEmptyContext) {
                 *     logger.debug("Empty context is allowed. Returning the original query.");
                 *     return query;
                 * }
                 * 如果该参数设置为true 则不会调用用户自定义的模板内容
                 * 而是调用ai回答该问题 所以这里得配置为false
                 */
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
