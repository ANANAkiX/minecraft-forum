package com.minecraftforum.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    private Boolean isLiked; // 当前用户是否已点赞
    private Integer replyCount; // 回复数量
    private List<ReplyDTO> replies; // 子回复列表（树形结构，可选，用于展开时加载）
}

