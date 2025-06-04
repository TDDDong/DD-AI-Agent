package com.dd.ddaiagent.app;

import com.dd.ddaiagent.advisor.MyLoggerAdvisor;
import com.dd.ddaiagent.rag.App.AppRagCustomAdvisorFactory;
import com.dd.ddaiagent.rag.common.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    public final ChatClient chatClient;

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 需要使用时再开启 避免启动应用时加载浪费额度
     */
    /*@Resource
    private Advisor loveAppRagCloudAdvisor;*/

    /*@Resource
    private VectorStore pgVectorVectorStore;*/

    @Resource
    private QueryRewriter queryRewriter;

    /*private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";*/



    public LoveApp(ChatModel dashScopeChatModel, @Qualifier("mySqlChatMemory") ChatMemory chatMemory,
                   @Value("classpath:/prompts/system-message.st") org.springframework.core.io.Resource systemResource) {
        //ChatMemory chatMemory = new InMemoryChatMemory();
        /*String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);*/
        // 直接使用资源创建模板
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Map<String, Object> variables = new HashMap<>();
        variables.put("role", "恋爱心理");
        variables.put("userNeeds", "单身、恋爱、已婚三种状态");
        variables.put("requirement", "单身状态询问社交圈拓展及追求心仪对象的困扰；" +
                "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。");
        variables.put("guide", "详述事情经过、对方反应及自身想法");
        String SYSTEM_PROMPT = systemPromptTemplate.createMessage(variables).getText();
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志记录拦截器 简化日志记录
                        new MyLoggerAdvisor()
                        //Re2(Re-Reading拦截器)
                        //,new ReReadingAdvisor()
                )
                .build();
    }

    public String doChat(String userMsg, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(userMsg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 流式输出的聊天
     */
    public Flux<String> doChatByStream(String userMsg, String chatId) {
        return chatClient.prompt()
                .user(userMsg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .stream()
                .content();
    }

    /**
     * 自定义返回实体类 用于接收ai返回的数据
     * @param title
     * @param suggestions
     */
    record LoveReport(String title, List<String> suggestions) {

    }


    /**
     * 按指定实体类型输出结果的聊天
     */
    public LoveReport doChatWithReport(String userMsg, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .user(userMsg)
                //.system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    /**
     * 基于RAG实现的聊天
     */
    public String doChatWithRag(String message, String chatId) {
        //调用查询重写器重写
        //String rewritten = queryRewriter.doRewrite(message);
        ChatResponse chatResponse = chatClient.prompt()
                //应用重构后的内容
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                //应用 RAG 知识库问答
                //.advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                //应用 RAG 增强检索服务(云知识库服务)
                //.advisors(loveAppRagCloudAdvisor)
                //应用 RAG 增强检索服务(PgVector向量检索)
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .advisors(AppRagCustomAdvisorFactory.createAppRagCustomAdvisor(loveAppVectorStore, "已婚", "恋爱"))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 提供工具调用的聊天
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 提供MCP服务的聊天
     */
    public String doChatWithMCP(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}
