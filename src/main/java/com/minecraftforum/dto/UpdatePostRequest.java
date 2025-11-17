package com.minecraftforum.dto;

import lombok.Data;

/**
 * 更新帖子请求DTO
 */
@Data
public class UpdatePostRequest {
    private Long id;
    private String title;
    private String content;
    private String category;
}

