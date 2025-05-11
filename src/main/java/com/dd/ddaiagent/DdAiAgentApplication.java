package com.dd.ddaiagent;

import com.dd.ddaiagent.rag.PgVectorVectorStoreConfig;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class DdAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdAiAgentApplication.class, args);
    }

}
