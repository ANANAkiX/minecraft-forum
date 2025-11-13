package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String code; // 权限代码
    private String name; // 权限名称
    private String type; // 权限类型：PAGE-页面访问，ACTION-操作权限
    private String description; // 权限描述
    private Long parentId; // 父权限ID，0表示顶级权限
    private Integer sortOrder; // 排序顺序
    private Integer status; // 状态：0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

