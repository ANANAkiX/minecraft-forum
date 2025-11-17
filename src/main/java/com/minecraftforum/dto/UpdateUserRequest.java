package com.minecraftforum.dto;

import lombok.Data;

/**
 * 更新用户请求DTO
 */
@Data
public class UpdateUserRequest {
    private Long id;
    private String nickname;
    private String email;
    private Integer status;
}

