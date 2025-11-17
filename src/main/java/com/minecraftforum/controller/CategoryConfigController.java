package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import com.minecraftforum.entity.CategoryConfig;
import com.minecraftforum.service.CategoryConfigService;
import com.minecraftforum.dto.DeleteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类配置控制器
 * 处理资源分类和论坛分类的动态配置管理
 */
@Tag(name = "分类配置管理", description = "资源分类和论坛分类的动态配置接口")
@RestController
@RequestMapping("/api/category-config")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryConfigController {
    
    private final CategoryConfigService categoryConfigService;
    
    /**
     * 获取启用的分类配置（公开接口）
     */
    @Operation(summary = "获取启用的分类配置", description = "获取指定类型下所有启用的分类配置，用于前端展示")
    @GetMapping("/enabled")
    @AnonymousAccess
    public Result<List<CategoryConfig>> getEnabledConfigs(
            @Parameter(description = "分类类型：RESOURCE-资源分类，FORUM-论坛分类", example = "RESOURCE")
            @RequestParam(defaultValue = "RESOURCE") String type) {
        List<CategoryConfig> configs = categoryConfigService.getEnabledConfigs(type);
        return Result.success(configs);
    }
    
    /**
     * 获取所有分类配置（管理接口）
     */
    @Operation(summary = "获取所有分类配置", description = "获取所有分类配置，包括已禁用的，需要admin:category:manage权限")
    @GetMapping
    public Result<List<CategoryConfig>> getAllConfigs(
            @Parameter(description = "分类类型：RESOURCE-资源分类，FORUM-论坛分类")
            @RequestParam(required = false) String type) {
        List<CategoryConfig> configs = categoryConfigService.getAllConfigs(type);
        return Result.success(configs);
    }
    
    /**
     * 根据ID获取分类配置
     */
    @Operation(summary = "获取分类配置详情", description = "根据ID获取分类配置的详细信息，需要admin:category:manage权限")
    @GetMapping("/detail")
    public Result<CategoryConfig> getConfigById(
            @Parameter(description = "分类配置ID", required = true)
            @RequestParam Long id) {
        if (id == null) {
            return Result.error(400, "分类配置ID不能为空");
        }
        CategoryConfig config = categoryConfigService.getConfigById(id);
        if (config == null) {
            return Result.error(404, "分类配置不存在");
        }
        return Result.success(config);
    }
    
    /**
     * 创建分类配置
     */
    @Operation(summary = "创建分类配置", description = "创建新的分类配置，需要admin:category:manage权限")
    @PostMapping
    public Result<CategoryConfig> createConfig(
            @Parameter(description = "分类配置信息", required = true)
            @RequestBody CategoryConfig config) {
        CategoryConfig created = categoryConfigService.createConfig(config);
        return Result.success(created);
    }
    
    /**
     * 更新分类配置
     */
    @Operation(summary = "更新分类配置", description = "更新分类配置信息，需要admin:category:manage权限")
    @PutMapping
    public Result<CategoryConfig> updateConfig(
            @Parameter(description = "分类配置信息", required = true)
            @RequestBody CategoryConfig config) {
        if (config.getId() == null) {
            return Result.error(400, "分类配置ID不能为空");
        }
        CategoryConfig updated = categoryConfigService.updateConfig(config);
        return Result.success(updated);
    }
    
    /**
     * 删除分类配置
     */
    @Operation(summary = "删除分类配置", description = "删除分类配置，需要admin:category:manage权限")
    @DeleteMapping
    public Result<Void> deleteConfig(
            @RequestBody DeleteRequest request) {
        if (request.getId() == null) {
            return Result.error(400, "分类配置ID不能为空");
        }
        categoryConfigService.deleteConfig(request.getId());
        return Result.success(null);
    }
}
