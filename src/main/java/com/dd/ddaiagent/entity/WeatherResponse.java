package com.dd.ddaiagent.entity;

import lombok.Data;

import java.util.List;

@Data
public class WeatherResponse {
    /**
     * 返回状态 1：成功；0：失败
     */
    private String status;
    /**
     * 返回结果总数目
     */
    private String count;
    /**
     * 返回的状态信息
     */
    private String info;
    /**
     * 返回状态说明,10000代表正确
     */
    private String infocode;
    /**
     * 实况天气数据信息
     */
    private List<WeatherInfo> lives;

    @Data
    public static class WeatherInfo {
        /**
         * 省份名
         */
        private String province;
        /**
         * 城市名
         */
        private String city;
        /**
         * 区域编码
         */
        private String adcode;
        /**
         * 天气现象（汉字描述）
         */
        private String weather;
        /**
         * 实时气温，单位：摄氏度
         */
        private String temperature;
        /**
         * 风向描述
         */
        private String winddirection;
        /**
         * 风力级别，单位：级
         */
        private String windpower;
        /**
         * 空气湿度
         */
        private String humidity;
        /**
         * 数据发布的时间
         */
        private String reporttime;
    }
} 