package com.dd.ddaiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.ddaiagent.entity.POISearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.*;

/**
 * 高德周边搜索 - 基于经纬度为中心搜索半径范围内的指定类型的地址
 */
@Slf4j
public class SearchAroundTool {

    private final String aMapKey;


    public SearchAroundTool(String aMapKey) {
        this.aMapKey = aMapKey;
    }

    @Tool(description = "Search for points of interest (POIs) around a specified location within a given radius")
    public List<POISearchResponse> searchAroundPlace(@ToolParam(description = "Center point coordinates in 'longitude,latitude' format. Example: '113.271429,23.103838'") String location,
                                                     @ToolParam(description = "Search radius in meters, maximum 50000", required = false) Integer radius,
                                                     @ToolParam(description = "Optional. POI type code to filter results. Only accepts two values: " +
                                                                     "'050000' (Restaurants/Dining Services) or '110000' (Scenic Spots/Attractions). " +
                                                                     "Note: It's recommended to make separate API calls for each type rather than combining " +
                                                                     "multiple types, as this ensures more comprehensive results due to API result count limitations.",
                                                             required = false) String types
                                    ) {
        String url = "https://restapi.amap.com/v3/place/around";
        Map<String, Object> params = new HashMap<>();
        params.put("key", aMapKey);
        params.put("location", location);
        params.put("offset", 3);
        if (!Objects.isNull(radius)) {
            params.put("radius", radius);
        }
        if (StrUtil.isNotEmpty(types)) {
            params.put("types", types);
        }
        List<POISearchResponse> result = new ArrayList<>();
        try {
            String response = HttpUtil.get(url, params);
            JSONObject entries = JSONUtil.parseObj(response);
            // 检查状态码
            if ("0".equals(entries.getStr("status"))) {
                //当 status 为 0 时，info 会返回具体错误原因
                log.error("调用高德API搜索周边服务失败：" + entries.getStr("info"));
                return result;
            }
            // 只返回搜索 POI 信息列表
            List<JSONObject> poiList = entries.getBeanList("pois", JSONObject.class);
            if (poiList == null || poiList.isEmpty()) {
                log.error("调用高德API搜索周边服务为空");
                return result;
            }
            // 将查询所得列表转换为对应实体类
            result = poiList.stream().map(POISearchResponse::new).toList();
        } catch (Exception e) {
            log.error("搜索周边服务失败，错误信息：{}", e.getMessage());
        }
        return result;
    }
}
