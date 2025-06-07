package com.dd.ddaiagent.app;

import com.dd.ddaiagent.advisor.MyLoggerAdvisor;
import com.dd.ddaiagent.rag.App.AppRagCustomAdvisorFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Component
@Slf4j
public class TravelApp {

    private final ChatClient chatClient;

    @Resource
    private VectorStore travelAppVectorStore;


    public TravelApp(ChatModel dashscopeChatModel, @Qualifier("mySqlChatMemory")ChatMemory chatMemory,
                     @Value("classpath:prompts/system-message.st") org.springframework.core.io.Resource systemResource) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Map<String, Object> variables = new HashMap<>();
        variables.put("role", "旅游规划");
        variables.put("userNeeds", "美食、景点、住宿三种需求");
        variables.put("requirement", "美食需求询问用户是否有特别想吃的菜系或当地特色美食；" +
                "景点需求询问更倾向热门大众景点，还是小众独特景点；" +
                "住宿需求询问用户对酒店星级、地理位置、房间特色、价格区间的要求。");
        variables.put("guide", "详述需求细节、期望达成的旅行效果等");
        String SYSTEM_PROMPT = systemPromptTemplate.createMessage(variables).getText();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    //基于RAG实现本地知识库聊天
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(
                        new MyLoggerAdvisor(),
                        AppRagCustomAdvisorFactory.createAppRagCustomAdvisor(travelAppVectorStore, "美食", "旅游规划")
                )
                .call()
                .chatResponse();
        String text = chatResponse.getResult().getOutput().getText();
        log.info("text: {}", text);
        return text;
    }
}
