package com.minecraftforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("resource_tag")
public class ResourceTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long resourceId;
    private String tagName;
}





