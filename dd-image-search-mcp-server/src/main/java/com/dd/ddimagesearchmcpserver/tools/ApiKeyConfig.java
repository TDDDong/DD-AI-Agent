package com.dd.ddimagesearchmcpserver.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-key.properties")
public class ApiKeyConfig {
    
    @Value("${api.key}")
    private String apikey;

    public String getApikey() {
        return apikey;
    }
}
