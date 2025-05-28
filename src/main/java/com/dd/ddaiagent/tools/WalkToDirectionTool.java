package com.dd.ddaiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.ddaiagent.entity.POISearchResponse;
import com.dd.ddaiagent.entity.WalkingRouteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.*;

/**
 * 高德步行路径规划 - 规划100km 以内的步行通勤方案，并且返回通勤方案的数据
 */
@Slf4j
public class WalkToDirectionTool {

    private String aMapKey;

    public WalkToDirectionTool(String aMapKey) {
        this.aMapKey = aMapKey;
    }

    @Tool(description = "Plan a walking route between two locations and get detailed navigation instructions")
    public List<WalkingRouteResponse> planWalkingRoute(
            @ToolParam(description = "Starting point coordinates in 'longitude,latitude' format. Example: '117.500244,40.417801'. " +
                            "Decimal points should not exceed 6 digits")
            String origin,
            @ToolParam(description = "Destination coordinates in 'longitude,latitude' format. Example: '117.500244,40.417801'. " +
                            "Decimal points should not exceed 6 digits")
            String destination
    ) {
        String url = "https://restapi.amap.com/v3/direction/walking";
        Map<String, Object> params = new HashMap<>();
        params.put("key", aMapKey);
        params.put("origin", origin);
        params.put("destination", destination);
        List<WalkingRouteResponse> result = new ArrayList<>();
        try {
            String response = HttpUtil.get(url, params);
            JSONObject entries = JSONUtil.parseObj(response);
            // 检查状态码
            if ("0".equals(entries.getStr("status"))) {
                //当 status 为 0 时，info 会返回具体错误原因
                log.error("调用高德API进行步行规划失败：" + entries.getStr("info"));
                return result;
            }
            // 只返回路线信息列表
            JSONObject route = entries.getBean("route", JSONObject.class);
            if (route == null || route.isEmpty()) {
                log.error("调用高德API进行步行规划结果为空");
                return result;
            }
            List<JSONObject> pathList = route.getBeanList("paths", JSONObject.class);
            // 将查询所得列表转换为对应实体类
            result = pathList.stream().map(WalkingRouteResponse::new).toList();
        } catch (Exception e) {
            log.error("步行规划失败，错误信息：{}", e.getMessage());
        }
        return result;
    }
}
