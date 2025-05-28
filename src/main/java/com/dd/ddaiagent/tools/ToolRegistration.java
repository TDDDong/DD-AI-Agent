package com.dd.ddaiagent.tools;

import com.dd.ddaiagent.task.CityCodeInitTask;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Value("${zhipu.api-key}")
    private String zhiPuApiKey;

    @Value("${amap.key}")
    private String aMapKey;

    @Resource
    private CityCodeInitTask cityCodeInitTask;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        //WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        ZhiPuWebSearchTool zhiPuWebSearchTool = new ZhiPuWebSearchTool(zhiPuApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        WeatherQueryTool weatherQueryTool = new WeatherQueryTool(aMapKey, cityCodeInitTask);
        TerminateTool terminateTool = new TerminateTool();
        GeoCodeTool geoCodeTool = new GeoCodeTool(aMapKey);
        SearchAroundTool searchAroundTool = new SearchAroundTool(aMapKey);
        return ToolCallbacks.from(
            fileOperationTool,
            //webSearchTool,
            zhiPuWebSearchTool,
            webScrapingTool,
            resourceDownloadTool,
            terminalOperationTool,
            pdfGenerationTool,
            weatherQueryTool,
            terminateTool,
            geoCodeTool,
            searchAroundTool
        );
    }
}
