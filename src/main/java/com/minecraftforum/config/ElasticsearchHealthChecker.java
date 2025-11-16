package com.minecraftforum.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Elasticsearch 健康检查和自动重连机制
 * 当连接失败时，静默处理，不影响主程序运行
 */
@Slf4j
@Component
public class ElasticsearchHealthChecker {
    
    private final ElasticsearchClient elasticsearchClient;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isChecking = new AtomicBoolean(false);
    
    /**
     * 构造函数，使用 Optional 处理 ElasticsearchClient 可能不存在的情况
     */
    public ElasticsearchHealthChecker(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }
    
    /**
     * 检查 Elasticsearch 是否可用
     */
    public boolean isAvailable() {
        return isConnected.get();
    }
    
    /**
     * 检查连接状态
     */
    @Async
    public void checkConnection() {
        // 防止并发检查
        if (isChecking.getAndSet(true)) {
            return;
        }
        
        try {
            // 检查 ElasticsearchClient 是否可用
            if (elasticsearchClient == null) {
                log.debug("ElasticsearchClient 未初始化，跳过连接检查");
                isConnected.set(false);
                return;
            }
            
            // 尝试执行一个简单的健康检查
            elasticsearchClient.cluster().health(HealthRequest.of(h -> h));
            if (!isConnected.get()) {
                log.info("Elasticsearch 连接成功");
            }
            isConnected.set(true);
        } catch (Exception e) {
            // 静默处理连接失败
            if (isConnected.get()) {
                // 从连接状态变为断开，记录警告
                log.warn("Elasticsearch 连接断开，将静默重试: {}", e.getMessage());
            } else {
                // 持续未连接状态，只记录调试日志（避免日志过多）
                log.debug("Elasticsearch 连接失败，将静默重试: {}", e.getMessage());
            }
            isConnected.set(false);
        } finally {
            isChecking.set(false);
        }
    }
    
    /**
     * 应用启动后检查连接（延迟执行，避免阻塞启动）
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onApplicationReady() {
        // 延迟5秒后检查，给 Elasticsearch 一些启动时间
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        log.info("开始检查 Elasticsearch 连接状态...");
        checkConnection();
    }
    
    /**
     * 定期检查连接状态（每30秒检查一次）
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void scheduledHealthCheck() {
        if (!isConnected.get()) {
            // 如果未连接，尝试重连
            checkConnection();
        }
    }
}

