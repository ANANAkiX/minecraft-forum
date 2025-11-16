package com.minecraftforum.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Elasticsearch 配置类
 * 配置 ElasticsearchClient 支持 Java 8 时间类型
 */
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
        
        // 创建 RestClient
        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, scheme)
        ).build();
        
        // 创建 Transport
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, jsonpMapper);
        
        // 创建并返回 ElasticsearchClient
        return new ElasticsearchClient(transport);
    }
}

