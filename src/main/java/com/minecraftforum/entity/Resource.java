package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String description;
    private String content; // Markdown 内容
    private String category; // PACK, MOD, RESOURCE
    private String version;
    private Long authorId;
    private String fileUrl;
    private String thumbnailUrl;
    private Integer downloadCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

