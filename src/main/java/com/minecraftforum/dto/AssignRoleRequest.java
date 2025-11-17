package com.minecraftforum.dto;

import lombok.Data;

/**
 * 分配角色请求DTO
 */
@Data
public class AssignRoleRequest {
    private Long userId;
    private Long roleId;
}

