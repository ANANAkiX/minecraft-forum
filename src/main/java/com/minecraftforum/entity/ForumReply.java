package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_reply")
public class ForumReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long commentId; // 所属评论ID
    private Long parentId; // 父回复ID，用于支持嵌套回复（如果为null，则是直接回复评论）
    private Long authorId;
    private Long targetUserId; // 被回复的用户ID
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
}






