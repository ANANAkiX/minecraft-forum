package com.minecraftforum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * CORS 跨域配置类
 * 从 application.yml 中读取 CORS 相关配置
 */
@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsConfig {
    
    /**
     * 允许的源（域名列表）
     */
    private List<String> allowedOrigins;
    
    /**
     * 允许的 HTTP 方法
     */
    private List<String> allowedMethods;
    
    /**
     * 允许的请求头
     */
    private List<String> allowedHeaders;
    
    /**
     * 是否允许携带凭证（Cookie、Authorization 等）
     */
    private Boolean allowCredentials = true;
    
    /**
     * 预检请求的缓存时间（秒）
     */
    private Long maxAge = 3600L;
}

