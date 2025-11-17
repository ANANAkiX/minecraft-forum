package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String content;
    private String category; // SHARE, HELP, TUTORIAL, ANNOUNCEMENT
    private Long authorId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String status; // NORMAL, LOCKED, DELETED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}













