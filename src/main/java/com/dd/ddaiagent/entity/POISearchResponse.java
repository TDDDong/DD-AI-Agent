package com.dd.ddaiagent.entity;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储高德周边搜索结果
 */
@Data
@NoArgsConstructor
public class POISearchResponse {
    /**
     * 名称
     */
    private String name;
    /**
     * 地址
     */
    private String address;
    /**
     * 经纬度
     */
    private String location;
    /**
     * 评分 仅存在于餐饮、酒店、景点、影院类 POI 之下
     */
    private String rating;
    /**
     * 人均消费 仅存在于餐饮、酒店、景点、影院类 POI 之下
     */
    private String cost;
    /**
     * 图片列表
     */
    private List<String> photoUrl;

    public POISearchResponse(JSONObject poi) {
        this.name = poi.getStr("name");
        this.address = poi.getStr("address");
        this.location = poi.getStr("location");
        this.photoUrl = poi.getJSONArray("photos").stream()
                .map(photo -> {
                    JSONObject obj = (JSONObject) photo;
                    return obj.getStr("url");
                }).collect(Collectors.toList());
        JSONObject bizExt = poi.getJSONObject("biz_ext");
        this.rating = bizExt.getStr("rating");
        this.cost = bizExt.getStr("cost");
    }
}
