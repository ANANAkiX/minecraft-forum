package com.minecraftforum.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Elasticsearch 配置类
 * 配置 ElasticsearchClient 支持 Java 8 时间类型
 * 连接失败时不影响应用启动，由健康检查器负责重连
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {
    
    private final ElasticsearchProperties elasticsearchProperties;
    
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ElasticsearchClient elasticsearchClient() {
        // 创建 ObjectMapper 并注册 JavaTimeModule 以支持 LocalDateTime
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳，使用 ISO-8601 格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 创建 JacksonJsonpMapper
        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);
        
        // 解析 Elasticsearch URI
        String uris = elasticsearchProperties.getUris() != null ? 
                elasticsearchProperties.getUris() : "http://localhost:9200";
        String[] uriParts = uris.replace("http://", "").replace("https://", "").split(":");
        String host = uriParts[0];
        int port = uriParts.length > 1 ? Integer.parseInt(uriParts[1]) : 9200;
        
        // 从配置中读取协议，如果没有配置则从 URI 中提取
        String scheme = elasticsearchProperties.getScheme();
        if (scheme == null || scheme.isEmpty()) {
            scheme = uris.startsWith("https://") ? "https" : "http";
        }
        
        try {
            // 创建 RestClient，设置连接超时和重试策略
            RestClient restClient = RestClient.builder(
                    new HttpHost(host, port, scheme)
            )
            .setRequestConfigCallback(requestConfigBuilder -> {
                // 设置连接超时
                int connectionTimeout = parseTimeout(elasticsearchProperties.getConnectionTimeout());
                int socketTimeout = parseTimeout(elasticsearchProperties.getSocketTimeout());
                return requestConfigBuilder
                        .setConnectTimeout(connectionTimeout)
                        .setSocketTimeout(socketTimeout);
            })
            .build();
            
            // 创建 Transport
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, jsonpMapper);
            
            // 创建并返回 ElasticsearchClient
            // 注意：即使 Elasticsearch 服务不可用，客户端对象也能创建成功
            // 实际的连接检查由 ElasticsearchHealthChecker 负责
            log.info("Elasticsearch 客户端创建成功，连接状态将由健康检查器监控");
            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            // 如果创建客户端本身失败（如配置错误），记录警告并抛出异常
            log.error("Elasticsearch 客户端创建失败: {}", e.getMessage(), e);
            throw new RuntimeException("Elasticsearch 客户端创建失败", e);
        }
    }
    
    /**
     * 解析超时时间字符串（如 "5s" -> 5000ms）
     */
    private int parseTimeout(String timeoutStr) {
        if (timeoutStr == null || timeoutStr.isEmpty()) {
            return 5000; // 默认5秒
        }
        try {
            String trimmed = timeoutStr.trim().toLowerCase();
            if (trimmed.endsWith("s")) {
                return Integer.parseInt(trimmed.substring(0, trimmed.length() - 1)) * 1000;
            } else if (trimmed.endsWith("ms")) {
                return Integer.parseInt(trimmed.substring(0, trimmed.length() - 2));
            } else {
                return Integer.parseInt(trimmed);
            }
        } catch (Exception e) {
            return 5000; // 默认5秒
        }
    }
}

