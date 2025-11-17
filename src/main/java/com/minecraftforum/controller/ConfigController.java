package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.config.ForumConfig;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置控制器
 * 提供前端获取系统配置的接口
 */
@Tag(name = "配置管理", description = "获取系统配置信息")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ForumConfig forumConfig;

    /**
     * 获取系统配置
     * 允许匿名访问，前端需要知道是否允许匿名访问首页和论坛
     */
    @Operation(summary = "获取系统配置", description = "获取系统配置信息，包括是否允许匿名访问、是否开启Elasticsearch搜索等")
    @GetMapping("/system")
    @AnonymousAccess
    public Result<Map<String, Object>> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("anonymousAccess", forumConfig.getAnonymousAccess() != null && forumConfig.getAnonymousAccess());
        config.put("elasticsearchEnabled", forumConfig.getElasticsearchEnabled() != null && forumConfig.getElasticsearchEnabled());
        return Result.success(config);
    }
}


