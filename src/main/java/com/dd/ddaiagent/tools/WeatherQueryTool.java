package com.dd.ddaiagent.tools;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.dd.ddaiagent.task.CityCodeInitTask;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于高德API实现的的实时天气查询工具
 */
@Slf4j
public class WeatherQueryTool {

    private final String aMapKey;

    private CityCodeInitTask cityCodeInitTask;

    public WeatherQueryTool(String aMapKey, CityCodeInitTask cityCodeInitTask) {
        this.aMapKey = aMapKey;
        this.cityCodeInitTask = cityCodeInitTask;
    }

    @Tool(description = "Query real-time weather information for a specified city")
    public String queryWeather(@ToolParam(description = "The name of the city to query real-time weather information.") String cityName) {
        String adCodeOfCity = cityCodeInitTask.getCityCode(cityName);
        if (adCodeOfCity == null) {
            log.info("未找到城市: {} 的城市编码", cityName);
            return "未找到" + cityName + "对应的城市编码";
        }
        // 构建请求URL
        String url = "https://restapi.amap.com/v3/weather/weatherInfo";
        Map<String, Object> params = new HashMap<>();
        params.put("key", aMapKey);
        params.put("city", adCodeOfCity);
        params.put("extensions", "base");
        params.put("output", "JSON");

        try {
            // 发送HTTP请求
            String response = HttpUtil.get(url, params);

            // 解析响应
            JSONObject jsonResponse = JSONUtil.parseObj(response);

            // 检查状态码
            if (!"1".equals(jsonResponse.getStr("status"))) {
                return "调用高德API查询天气失败";
            }

            // 只返回天气状况字段
            List<JSONObject> lives = jsonResponse.getBeanList("lives", JSONObject.class);
            if (lives == null || lives.isEmpty()) {
                return "查询不到该城市的天气数据";
            }

            // 仅返回天气状况字段
            return lives.get(0).getStr("weather");

        } catch (Exception e) {
            log.error("查询天气失败，错误信息：{}", e.getMessage());
            return "调用高德API查询天气出现异常";
        }
    }
}
