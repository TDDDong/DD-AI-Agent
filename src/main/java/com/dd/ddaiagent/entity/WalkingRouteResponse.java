package com.dd.ddaiagent.entity;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储高德起点到终点的步行方案
 */
@NoArgsConstructor
@Data
public class WalkingRouteResponse {
    /**
     * 起点和终点的步行距离 单位：米
     */
    private String distance;
    /**
     * 步行时间预计 单位：秒
     */
    private String duration;
    /**
     * 步行结果列表
     */
    private List<StepVO> steps;


    @Data
    @NoArgsConstructor
    private static class StepVO {
        /**
         * 路段步行指示
         */
        private String instruction;
        /**
         * 此路段距离 单位：米
         */
        private String distance;
        /**
         * 此路段预计步行时间
         */
        private String duration;

        public StepVO(Object obj) {
            JSONObject step = (JSONObject) obj;
            this.instruction = step.getStr("instruction");
            this.distance = step.getStr("distance");
            this.duration = step.getStr("duration");
        }
    }

    public WalkingRouteResponse(JSONObject path) {
        this.distance = path.getStr("distance");
        this.duration = path.getStr("duration");
        JSONArray array = path.getJSONArray("steps");
        this.steps = array.stream().map(StepVO::new).collect(Collectors.toList());
    }
}
