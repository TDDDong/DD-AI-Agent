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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于高德API实现的的实时天气查询工具
 */
@Slf4j
@Component
public class WeatherQueryTool {

    // 使用Environment代替直接@Value注入
    @Autowired
    private Environment environment;

    private String getAmapKey() {
        return environment.getProperty("amap.key");
    }

    @Value("${amap.key}")
    private String amapKey;

    private static Map<String, String> cityCodeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            // 读取Excel文件
            InputStream inputStream = ResourceUtil.getStream("citycode/AMap_adcode_citycode.xlsx");
            ExcelReader reader = ExcelUtil.getReader(inputStream);

            // 读取所有行数据
            List<Map<String, Object>> rows = reader.readAll();

            // 遍历并解析数据
            for (Map<String, Object> row : rows) {
                String cityName = row.get("中文名") != null ? row.get("中文名").toString() : "";
                String adcode = row.get("adcode") != null ? row.get("adcode").toString() : "";

                if (!cityName.isEmpty() && !adcode.isEmpty()) {
                    cityCodeMap.put(cityName, adcode);
                }
            }
        } catch (Exception e) {
            log.error("初始化城市编码映射表失败, errorMsg:{}", e.getMessage());
        }
    }

    @Tool(description = "Query real-time weather information for a specified city")
    public String queryWeather(@ToolParam(description = "The name of the city to query real-time weather information.") String cityName) {
        String adCodeOfCity = cityCodeMap.getOrDefault(cityName, null);
        if (adCodeOfCity == null) {
            log.info("未找到城市: {} 的城市编码", cityName);
            return "未找到" + cityName + "对应的城市编码";
        }
        // 构建请求URL
        String url = "https://restapi.amap.com/v3/weather/weatherInfo";
        Map<String, Object> params = new HashMap<>();
        params.put("key", getAmapKey());
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
