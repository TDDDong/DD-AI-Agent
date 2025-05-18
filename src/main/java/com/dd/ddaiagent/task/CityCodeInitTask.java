package com.dd.ddaiagent.task;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于启动时读取资源目录下的excel表格并解析生成城市名称与城市编码的对应关系
 */
@Component
@Slf4j
public class CityCodeInitTask {

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

    /**
     * 获取城市对应的编码
     * @param cityName 城市名称
     * @return 城市编码，若不存在则返回null
     */
    public String getCityCode(String cityName) {
        return cityCodeMap.getOrDefault(cityName, null);
    }

    /**
     * 判断城市是否存在
     * @param cityName 城市名称
     * @return 是否存在
     */
    public boolean cityExists(String cityName) {
        return cityCodeMap.containsKey(cityName);
    }

}
