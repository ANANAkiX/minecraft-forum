package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minecraftforum.entity.CategoryConfig;
import com.minecraftforum.mapper.CategoryConfigMapper;
import com.minecraftforum.service.CategoryConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryConfigServiceImpl implements CategoryConfigService {
    
    private final CategoryConfigMapper categoryConfigMapper;
    
    @Override
    public List<CategoryConfig> getEnabledConfigs(String type) {
        LambdaQueryWrapper<CategoryConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryConfig::getType, type);
        wrapper.eq(CategoryConfig::getStatus, 1);
        wrapper.orderByAsc(CategoryConfig::getSortOrder);
        return categoryConfigMapper.selectList(wrapper);
    }
    
    @Override
    public List<CategoryConfig> getAllConfigs(String type) {
        LambdaQueryWrapper<CategoryConfig> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(CategoryConfig::getType, type);
        }
        wrapper.orderByAsc(CategoryConfig::getSortOrder);
        return categoryConfigMapper.selectList(wrapper);
    }
    
    @Override
    public CategoryConfig getConfigById(Long id) {
        return categoryConfigMapper.selectById(id);
    }
    
    @Override
    public CategoryConfig createConfig(CategoryConfig config) {
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getSortOrder() == null) {
            config.setSortOrder(0);
        }
        if (config.getIsDefault() == null) {
            config.setIsDefault(0);
        }
        categoryConfigMapper.insert(config);
        return config;
    }
    
    @Override
    public CategoryConfig updateConfig(CategoryConfig config) {
        config.setUpdateTime(LocalDateTime.now());
        categoryConfigMapper.updateById(config);
        return config;
    }
    
    @Override
    public void deleteConfig(Long id) {
        categoryConfigMapper.deleteById(id);
    }
}

