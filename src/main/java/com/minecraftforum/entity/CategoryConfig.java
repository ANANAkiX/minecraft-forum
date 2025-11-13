package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category_config")
public class CategoryConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name; // 分类名称（显示名称）
    private String code; // 分类代码（用于查询，空字符串表示"全部"）
    private String type; // 类型：RESOURCE-资源分类
    private Integer sortOrder; // 排序顺序
    private Integer isDefault; // 是否默认显示：0-否，1-是（全部）
    private Integer status; // 状态：0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

