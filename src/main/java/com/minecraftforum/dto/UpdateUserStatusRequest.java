package com.minecraftforum.dto;

import lombok.Data;

/**
 * 更新用户状态请求DTO
 */
@Data
public class UpdateUserStatusRequest {
    private Long userId;
    private Integer status;
}

