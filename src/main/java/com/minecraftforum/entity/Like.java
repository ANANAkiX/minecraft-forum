package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`like`")
public class Like {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long resourceId; // 可为空，用于资源点赞
    private Long postId; // 可为空，用于帖子点赞
    private Long commentId; // 可为空，用于评论点赞
    private Long replyId; // 可为空，用于回复点赞
    private LocalDateTime createTime;
}

