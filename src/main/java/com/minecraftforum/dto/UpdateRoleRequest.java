package com.minecraftforum.dto;

import lombok.Data;

/**
 * 更新角色请求DTO
 */
@Data
public class UpdateRoleRequest {
    private Long id;
    private String name;
    private String description;
    private Integer status;
}

