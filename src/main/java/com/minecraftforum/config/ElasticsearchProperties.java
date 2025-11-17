package com.minecraftforum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 配置属性类
 * 从 application.yml 中读取 Elasticsearch 相关配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.elasticsearch")
@Data
public class ElasticsearchProperties {
    
    /**
     * Elasticsearch 服务器地址（URI）
     */
    private String uris = "http://localhost:9200";
    
    /**
     * 协议：http 或 https
     */
    private String scheme = "http";
    
    /**
     * 连接超时时间
     */
    private String connectionTimeout = "5s";
    
    /**
     * Socket 超时时间
     */
    private String socketTimeout = "60s";
}


