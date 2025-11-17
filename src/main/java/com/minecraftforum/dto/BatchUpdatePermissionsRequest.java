package com.minecraftforum.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量更新权限请求DTO
 */
@Data
public class BatchUpdatePermissionsRequest {
    private Long userId;
    private List<Long> permissionIds;
}

