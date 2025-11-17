package com.minecraftforum.dto;

import lombok.Data;

/**
 * 获取评论列表请求DTO
 */
@Data
public class GetCommentsRequest {
    private Long postId;
    private Integer page;
    private Integer pageSize;
}

