package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_permission")
public class UserPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long permissionId;
    private LocalDateTime createTime;
}

