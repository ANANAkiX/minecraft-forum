package com.minecraftforum.dto;

import lombok.Data;

/**
 * 为角色分配权限请求DTO
 */
@Data
public class AssignRolePermissionRequest {
    private Long roleId;
    private Long permissionId;
}

