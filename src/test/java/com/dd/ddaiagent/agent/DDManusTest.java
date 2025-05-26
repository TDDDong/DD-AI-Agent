package com.dd.ddaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DDManusTest {

    @Resource
    private DDManus ddManus;

    @Test
    void run() {
        /*String userPrompt = """
                我的另一半居住在广州市海珠区，请帮我找到 5 公里内合适的约会地点，  
                并结合一些网络图片，制定一份详细的约会计划，  
                并以 PDF 格式输出""";*/
        String userPrompt = """  
                我计划未来两天到广州市海珠区旅游，请帮我找到 5 公里内合适的游玩景点，  
                并结合一些网络图片，制定一份详细的旅游规划，  
                并以 PDF 格式输出""";
        String answer = ddManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }

}