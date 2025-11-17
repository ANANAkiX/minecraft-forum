package com.minecraftforum.dto;

import lombok.Data;

/**
 * 操作请求DTO（通用，用于点赞、收藏、下载等操作）
 */
@Data
public class ActionRequest {
    private Long id; // 资源ID、帖子ID、评论ID等
}

