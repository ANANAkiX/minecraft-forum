package com.minecraftforum.dto;

import lombok.Data;

/**
 * 移除角色请求DTO
 */
@Data
public class RemoveRoleRequest {
    private Long userId;
    private Long roleId;
}

