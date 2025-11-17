package com.minecraftforum.dto;

import lombok.Data;

import java.util.List;

/**
 * 搜索结果DTO
 */
@Data
public class SearchResultDTO {
    
    /**
     * 类型：POST（帖子）或 RESOURCE（资源）
     */
    private String type;
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 标题（高亮后的）
     */
    private String title;
    
    /**
     * 内容摘要（高亮后的）
     */
    private String summary;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 作者名称
     */
    private String authorName;
    
    /**
     * 创建时间
     */
    private String createTime;
    
    /**
     * 高亮片段列表
     */
    private List<String> highlights;
}



