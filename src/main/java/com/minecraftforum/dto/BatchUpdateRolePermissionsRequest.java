package com.minecraftforum.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量更新角色权限请求DTO
 */
@Data
public class BatchUpdateRolePermissionsRequest {
    private Long roleId;
    private List<Long> permissionIds;
}

