package com.dd.ddaiagent.tools;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ZhiPuWebSearchTool {

    // 智谱Web Search API地址
    private static final String SEARCH_API_URL = "https://open.bigmodel.cn/api/paas/v4/web_search";

    private String apiKey;


    public ZhiPuWebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from ZhiPu Search Engine")
    public String webSearch(@ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("search_engine", "search_std");
        paramMap.put("search_query", query);
        try {
            HttpResponse response = HttpRequest.post(SEARCH_API_URL)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(JSONUtil.toJsonStr(paramMap))
                    .execute();
            JSONObject entries = JSONUtil.parseObj(response.body());
            JSONArray searchResult = entries.getJSONArray("search_result");
            if (Objects.isNull(searchResult)) {
                return "Search empty with query:" + query;
            }
            //提取前五条数据
            List<Object> objects = searchResult.subList(0, 5);
            //拼接搜索结果为字符串
            String result = objects.stream().map(obj -> {
                JSONObject temp = (JSONObject) obj;
                return temp.toString();
            }).collect(Collectors.joining(","));
            return result;
        } catch (HttpException e) {
            return "Error searching ZhiPu: " + e.getMessage();
        }
    }
}
