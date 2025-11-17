package com.minecraftforum.dto;

import lombok.Data;

/**
 * 获取资源文件列表请求DTO
 */
@Data
public class GetFilesByResourceRequest {
    private Long resourceId;
}

