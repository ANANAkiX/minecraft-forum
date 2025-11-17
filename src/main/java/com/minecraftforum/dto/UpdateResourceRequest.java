package com.minecraftforum.dto;

import lombok.Data;

/**
 * 更新资源请求DTO
 */
@Data
public class UpdateResourceRequest {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String category;
    private String version;
    private String tags;
}

