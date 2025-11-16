package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("download_log")
public class DownloadLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long resourceId;
    private LocalDateTime createTime;
}












