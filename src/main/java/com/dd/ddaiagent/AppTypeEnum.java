package com.dd.ddaiagent;

/**
 * 用于存储前端url中app类型与后端app应用名称的映射关系
 */
public enum AppTypeEnum {

    LOVE_APP("love_app", "LoveApp"),

    TRAVEL_APP("travel_app", "TravelApp");

    private final String appType;

    private final String appName;

    AppTypeEnum(String appType, String appName) {
        this.appType = appType;
        this.appName = appName;
    }

    public String getAppType() {
        return appType;
    }

    public String getAppName() {
        return appName;
    }

    public static String findNameByType(String appType) {
        for (AppTypeEnum appTypeEnum : values()) {
            if (appTypeEnum.getAppType().equals(appType)) {
                return appTypeEnum.getAppName();
            }
        }
        return null;
    }
}
