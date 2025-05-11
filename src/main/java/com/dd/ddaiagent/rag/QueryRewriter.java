package com.dd.ddaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 基于AI的查询重写器
 * 可以使用不同模型 避免费用过高
 */
@Component
public class QueryRewriter {


    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashScopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashScopeChatModel);
        queryTransformer = RewriteQueryTransformer
                .builder()
                .chatClientBuilder(builder)
                .build();
    }

    public String doRewrite(String prompt) {
        Query query = new Query(prompt);
        Query transformedQuery = queryTransformer.transform(query);
        return transformedQuery.text();
    }
}
