package com.dd.ddaiagent.rag.App;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

@Slf4j
public class AppRagCustomAdvisorFactory {

    public static Advisor createAppRagCustomAdvisor(VectorStore vectorStore, String status, String field) {
        //基于文档元信息中的status进行标签筛选
        Filter.Expression expression = new FilterExpressionBuilder().eq("status", status).build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) //过滤条件
                .similarityThreshold(0.5) //相似度阈值
                .topK(3) //返回文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever) //文档检索器
                .queryAugmenter(AppContextualQueryAugmenterFactory.createInstance(field)) //查询增强器
                .build();
    }
}
