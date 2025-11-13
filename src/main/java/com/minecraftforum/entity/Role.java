package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String code;
    private String description;
    private Integer status; // 0-禁用，1-启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

