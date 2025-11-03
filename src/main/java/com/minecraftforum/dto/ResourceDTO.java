package com.minecraftforum.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResourceDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String category;
    private String version;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private String fileUrl;
    private String thumbnailUrl;
    private Integer downloadCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> tags = new java.util.ArrayList<>();
}

