package com.minecraftforum.dto;

import lombok.Data;

/**
 * 获取用户评论列表请求DTO
 */
@Data
public class GetUserCommentsRequest {
    private Long userId;
}

