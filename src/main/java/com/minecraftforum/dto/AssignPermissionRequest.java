package com.minecraftforum.dto;

import lombok.Data;

/**
 * 分配权限请求DTO
 */
@Data
public class AssignPermissionRequest {
    private Long userId;
    private Long permissionId;
}

