package com.dd.ddaiagent.rag.loveApp;

import com.dd.ddaiagent.rag.common.MyKeywordEnricher;
import com.dd.ddaiagent.rag.common.MyTokenTextSplitter;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    /**
     * 将文档存储在本地内存中 作为向量数据
     * 需要使用时放开注释即会作为bean注入
     */
    //@Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        //加载所有文档
        List<Document> documents = loveAppDocumentLoader.loadMarkDowns();
        //自主切分文档
        //List<Document> splitDocuments = myTokenTextSplitter.splitDocuments(documents);
        //为文档添加元信息
        //List<Document> enrichDocuments = myKeywordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
