package com.minecraftforum.config;

import com.minecraftforum.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 权限缓存初始化器
 * 项目启动时自动加载权限数据到Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1) // 设置优先级，确保在其他初始化器之前执行
public class PermissionCacheInitializer implements CommandLineRunner {

    private final PermissionCacheService permissionCacheService;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("开始初始化权限缓存...");
            permissionCacheService.initPermissionCache();
            log.info("权限缓存初始化完成");
        } catch (Exception e) {
            log.error("权限缓存初始化失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}

