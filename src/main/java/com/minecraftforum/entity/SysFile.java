package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class SysFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属资源ID（可选，用于关联资源）
     */
    private Long resourceId;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 雪花算法生成的文件名
     */
    private String fileName;
    
    /**
     * 上传到 OSS 的访问 URL
     */
    private String fileUrl;
    
    /**
     * 文件大小（单位：字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型（例如：image/png）
     */
    private String fileType;
    
    /**
     * 创建人
     */
    private Long createUser;
    
    /**
     * 修改人
     */
    private Long updateUser;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}


