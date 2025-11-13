package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.config.AnonymousAccess;
import com.minecraftforum.entity.CategoryConfig;
import com.minecraftforum.service.CategoryConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-config")
@RequiredArgsConstructor
public class CategoryConfigController {
    
    private final CategoryConfigService categoryConfigService;
    
    @GetMapping("/enabled")
    @AnonymousAccess
    public Result<List<CategoryConfig>> getEnabledConfigs(@RequestParam(defaultValue = "RESOURCE") String type) {
        List<CategoryConfig> configs = categoryConfigService.getEnabledConfigs(type);
        return Result.success(configs);
    }
    
    @GetMapping
    public Result<List<CategoryConfig>> getAllConfigs(@RequestParam(required = false) String type) {
        List<CategoryConfig> configs = categoryConfigService.getAllConfigs(type);
        return Result.success(configs);
    }
    
    @GetMapping("/{id}")
    public Result<CategoryConfig> getConfigById(@PathVariable Long id) {
        CategoryConfig config = categoryConfigService.getConfigById(id);
        if (config == null) {
            return Result.error(404, "分类配置不存在");
        }
        return Result.success(config);
    }
    
    @PostMapping
    public Result<CategoryConfig> createConfig(@RequestBody CategoryConfig config) {
        CategoryConfig created = categoryConfigService.createConfig(config);
        return Result.success(created);
    }
    
    @PutMapping("/{id}")
    public Result<CategoryConfig> updateConfig(@PathVariable Long id, @RequestBody CategoryConfig config) {
        config.setId(id);
        CategoryConfig updated = categoryConfigService.updateConfig(config);
        return Result.success(updated);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        categoryConfigService.deleteConfig(id);
        return Result.success(null);
    }
}

