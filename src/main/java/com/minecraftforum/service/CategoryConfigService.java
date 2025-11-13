package com.minecraftforum.service;

import com.minecraftforum.entity.CategoryConfig;

import java.util.List;

public interface CategoryConfigService {
    List<CategoryConfig> getEnabledConfigs(String type);
    List<CategoryConfig> getAllConfigs(String type);
    CategoryConfig getConfigById(Long id);
    CategoryConfig createConfig(CategoryConfig config);
    CategoryConfig updateConfig(CategoryConfig config);
    void deleteConfig(Long id);
}

