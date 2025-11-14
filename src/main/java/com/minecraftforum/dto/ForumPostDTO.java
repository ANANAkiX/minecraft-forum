package com.minecraftforum.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumPostDTO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isLiked; // 当前用户是否已点赞
}




