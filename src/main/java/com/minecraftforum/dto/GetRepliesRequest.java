package com.minecraftforum.dto;

import lombok.Data;

/**
 * 获取评论的子回复列表请求DTO
 */
@Data
public class GetRepliesRequest {
    private Long commentId;
}

