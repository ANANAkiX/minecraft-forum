package com.minecraftforum.config;

import com.minecraftforum.service.ApiCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * API 扫描启动监听器
 * 根据配置决定是否在项目启动时加载 API 数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiScannerStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final ApiScannerConfig apiScannerConfig;
    private final ApiCacheService apiCacheService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (apiScannerConfig.getLoadOnStartup() != null && apiScannerConfig.getLoadOnStartup()) {
            log.info("API 扫描配置为启动时加载，开始扫描 API...");
            apiCacheService.scanAndCache();
        } else {
            log.info("API 扫描配置为懒加载，将在首次访问时加载");
        }
    }
}

