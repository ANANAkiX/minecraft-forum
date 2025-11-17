package com.minecraftforum.dto;

import lombok.Data;

/**
 * 更新权限请求DTO
 */
@Data
public class UpdatePermissionRequest {
    private Long id;
    private String code;
    private String name;
    private String type;
    private String description;
    private String router;
    private String apiurl;
    private String methodtype;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
}

