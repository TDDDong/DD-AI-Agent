package com.dd.ddaiagent.rag.App;


import cn.hutool.core.io.resource.ResourceUtil;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

public class AppContextualQueryAugmenterFactory {
    private static final Resource resource = new ClassPathResource("/prompts/empty-context.st");;

    public static ContextualQueryAugmenter createInstance(String field) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("field", field);
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate(resource, variables);
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
