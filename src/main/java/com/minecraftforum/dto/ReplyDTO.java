package com.minecraftforum.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReplyDTO {
    private Long id;
    private Long commentId;
    private Long parentId; // 父回复ID，用于支持嵌套回复
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private Long targetUserId; // 被回复的用户ID
    private String targetUserName; // 被回复的用户名
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    private Boolean isLiked; // 当前用户是否已点赞
    private List<ReplyDTO> children; // 子回复列表（嵌套结构）
}

