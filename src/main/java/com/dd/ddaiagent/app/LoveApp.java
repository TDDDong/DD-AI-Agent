package com.dd.ddaiagent.app;

import com.dd.ddaiagent.advisor.MyLoggerAdvisor;
import com.dd.ddaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.dd.ddaiagent.rag.QueryRewriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    public final ChatClient chatClient;

    @jakarta.annotation.Resource
    private VectorStore loveAppVectorStore;

    /**
     * 需要使用时再开启 避免启动应用时加载浪费额度
     */
    /*@jakarta.annotation.Resource
    private Advisor loveAppRagCloudAdvisor;*/

    /*@jakarta.annotation.Resource
    private VectorStore pgVectorVectorStore;*/

    @jakarta.annotation.Resource
    private QueryRewriter queryRewriter;

    /*private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";*/



    public LoveApp(ChatModel dashScopeChatModel, @Qualifier("redisChatMemory") ChatMemory chatMemory,
                   @Value("classpath:/prompts/system-message.st") Resource systemResource) {
        //ChatMemory chatMemory = new InMemoryChatMemory();
        /*String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);*/
        // 直接使用资源创建模板
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "恋爱心理");
        variables.put("notice", "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
                "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。");
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
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 自定义返回实体类 用于接收ai返回的数据
     * @param title
     * @param suggestions
     */
    record LoveReport(String title, List<String> suggestions) {

    }


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


    public String doChatWithRag(String message, String chatId) {
        //调用查询重写器重写
        String rewritten = queryRewriter.doRewrite(message);
        ChatResponse chatResponse = chatClient.prompt()
                //应用重构后的内容
                .user(rewritten)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                //应用 RAG 知识库问答
                //.advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                //应用 RAG 增强检索服务(云知识库服务)
                //.advisors(loveAppRagCloudAdvisor)
                //应用 RAG 增强检索服务(PgVector向量检索)
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, "已婚"))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
