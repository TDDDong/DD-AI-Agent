package com.dd.ddaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地理编码工具 - 用于将地点转换为经纬度坐标 可用于后续搜索周边景点等
 */
@Slf4j
public class GeoCodeTool {

    private final String aMapKey;

    public GeoCodeTool(String aMapKey) {
        this.aMapKey = aMapKey;
    }

    @Tool(description = "Convert a structured address to geographic coordinates (longitude and latitude)")
    public String geocodeAddress(
            @ToolParam(description = "Structured address following the format: country, province, city, district, town, " +
                    "village, street, street number, estate, building. Example: '北京市朝阳区阜通东大街6号'")
            String address
    ) {
        String url = "https://restapi.amap.com/v3/geocode/geo?parameters";
        Map<String, Object> params = new HashMap<>();
        params.put("key", aMapKey);
        params.put("address", address);
        try {
            String response = HttpUtil.get(url, params);
            JSONObject entries = JSONUtil.parseObj(response);
            // 检查状态码
            if ("0".equals(entries.getStr("status"))) {
                //当 status 为 0 时，info 会返回具体错误原因
                return "调用高德API转换地理编码失败：" + entries.getStr("info");
            }
            // 只返回地理编码信息列表
            List<JSONObject> geocodes = entries.getBeanList("geocodes", JSONObject.class);
            if (geocodes == null || geocodes.isEmpty()) {
                return "调用高德API转换地理编码为空";
            }
            // 仅返回坐标点内容 格式：经度，纬度
            return geocodes.get(0).getStr("location");
        } catch (Exception e) {
            log.error("地理编码转换失败，错误信息：{}", e.getMessage());
            return "调用高德API转换地理编码出现异常";
        }
    }
}
