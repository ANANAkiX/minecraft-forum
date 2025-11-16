package com.minecraftforum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * API 扫描配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "api-scanner")
public class ApiScannerConfig {
    /**
     * 是否在项目启动时加载
     * true: 启动时加载
     * false: 懒加载
     */
    private Boolean loadOnStartup = true;
    
    /**
     * 排除配置
     */
    private ExcludeConfig exclude = new ExcludeConfig();
    
    @Data
    public static class ExcludeConfig {
        /**
         * 排除的 Controller 名称列表（支持通配符匹配）
         * 例如：["*Controller", "Admin*"]
         */
        private List<String> controllers = new ArrayList<>();
        
        /**
         * 排除的方法名列表（支持通配符匹配）
         * 例如：["checkPermission", "get*", "*Info"]
         */
        private List<String> methods = new ArrayList<>();
    }
}

