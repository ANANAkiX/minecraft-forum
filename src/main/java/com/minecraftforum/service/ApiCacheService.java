package com.minecraftforum.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.util.ApiScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API 缓存服务
 * 负责将 API 扫描结果缓存到 Redis，并提供读取功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCacheService {

    private static final String REDIS_KEY = "api:scanner:list";
    private static final long CACHE_EXPIRE_HOURS = 24; // 缓存过期时间：24小时

    private final StringRedisTemplate redisTemplate;
    private final ApiScanner apiScanner;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从 Redis 获取 API 列表
     */
    public List<ApiScanner.ApiInfo> getApiListFromCache() {
        try {
            String json = redisTemplate.opsForValue().get(REDIS_KEY);
            if (json != null && !json.isEmpty()) {
                return objectMapper.readValue(json, new TypeReference<List<ApiScanner.ApiInfo>>() {});
            }
        } catch (Exception e) {
            log.error("从 Redis 读取 API 列表失败", e);
        }
        return null;
    }

    /**
     * 扫描 API 并缓存到 Redis
     */
    public List<ApiScanner.ApiInfo> scanAndCache() {
        try {
            log.info("开始扫描 API...");
            List<ApiScanner.ApiInfo> apiList = apiScanner.scanAllApis(applicationContext);
            
            // 缓存到 Redis
            String json = objectMapper.writeValueAsString(apiList);
            redisTemplate.opsForValue().set(REDIS_KEY, json, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("API 扫描完成，共扫描到 {} 个接口，已缓存到 Redis", apiList.size());
            return apiList;
        } catch (Exception e) {
            log.error("扫描并缓存 API 失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取 API 列表（优先从缓存读取，缓存不存在则扫描并缓存）
     */
    public List<ApiScanner.ApiInfo> getApiList() {
        List<ApiScanner.ApiInfo> apiList = getApiListFromCache();
        if (apiList == null || apiList.isEmpty()) {
            // 缓存不存在，重新扫描并缓存
            apiList = scanAndCache();
        }
        return apiList;
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        try {
            redisTemplate.delete(REDIS_KEY);
            log.info("API 缓存已清除");
        } catch (Exception e) {
            log.error("清除 API 缓存失败", e);
        }
    }
}


