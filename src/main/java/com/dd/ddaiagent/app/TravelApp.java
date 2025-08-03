package com.dd.ddaiagent.app;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
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
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Component("TravelApp")
@Slf4j
public class TravelApp implements AIAppStrategy {

    private final ChatClient chatClient;

    //@Resource
    private VectorStore travelAppVectorStore;

    @Resource
    private ToolCallback[] allTools;


    public TravelApp(ChatModel dashscopeChatModel, @Qualifier("mySqlChatMemory")ChatMemory chatMemory,
                     @Value("classpath:prompts/system-message.st") org.springframework.core.io.Resource systemResource,
                     ConfigurablePromptTemplateFactory configurablePromptTemplateFactory) {
        //SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Map<String, Object> variables = new HashMap<>();
        variables.put("role", "旅游规划");
        variables.put("userNeeds", "美食、景点、住宿三种需求");
        variables.put("requirement", "美食需求询问用户是否有特别想吃的菜系或当地特色美食；" +
                "景点需求询问更倾向热门大众景点，还是小众独特景点；" +
                "住宿需求询问用户对酒店星级、地理位置、房间特色、价格区间的要求。");
        variables.put("guide", "详述需求细节、期望达成的旅行效果等");
        //String SYSTEM_PROMPT = systemPromptTemplate.createMessage(variables).getText();
        ConfigurablePromptTemplate travelPrompt = configurablePromptTemplateFactory.create(
                "travelPrompt",
                systemResource,
                variables
        );
        String SYSTEM_PROMPT = travelPrompt.create().getContents();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    @Override
    public String doChat(String userMsg, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(userMsg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Override
    public Flux<String> doChatByStream(String userMsg, String chatId) {
        return chatClient.prompt()
                .user(userMsg)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    //基于RAG实现本地知识库聊天
    @Override
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
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
