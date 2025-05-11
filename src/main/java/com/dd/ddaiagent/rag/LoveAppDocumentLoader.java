package com.dd.ddaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 用于加载资源目录下的多个MarkDown格式文件
     */
    public List<Document> loadMarkDowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            //从资源目录下读取对应的markdown格式文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true) //将水平分割线用来分隔文档 形成多个文档
                        .withIncludeCodeBlock(false) //是否读取代码块
                        .withIncludeBlockquote(false) //是否读取引用块
                        .withAdditionalMetadata("filename", filename) //将文件名配置为额外的元信息
                        .withAdditionalMetadata("status", status)
                        .build();

                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败");
        }
        return allDocuments;
    }
}
