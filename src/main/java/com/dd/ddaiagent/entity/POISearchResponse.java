package com.dd.ddaiagent.entity;

import cn.hutool.json.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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
     * 图片列表
     */
    private List<String> photoUrl;

    public POISearchResponse(JSONObject poi) {
        this.name = poi.getStr("name");
        this.address = poi.getStr("address");
        this.photoUrl = poi.getJSONArray("photos").stream()
                .map(photo -> {
                    JSONObject obj = (JSONObject) photo;
                    return obj.getStr("url");
                }).collect(Collectors.toList());
    }
}
