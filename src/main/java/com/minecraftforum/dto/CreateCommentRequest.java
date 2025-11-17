package com.minecraftforum.dto;

import lombok.Data;

/**
 * 创建评论请求DTO
 */
@Data
public class CreateCommentRequest {
    private Long postId;
    private String content;
}

