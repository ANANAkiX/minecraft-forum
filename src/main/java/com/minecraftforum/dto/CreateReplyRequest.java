package com.minecraftforum.dto;

import lombok.Data;

/**
 * 创建回复请求DTO
 */
@Data
public class CreateReplyRequest {
    private Long commentId;
    private String content;
    private Long targetUserId;
    private Long parentId;
}

