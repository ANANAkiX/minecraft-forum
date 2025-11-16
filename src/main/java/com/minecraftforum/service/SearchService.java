package com.minecraftforum.service;

import com.minecraftforum.dto.SearchResultDTO;

import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 搜索帖子和资源
     * @param keyword 搜索关键词
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 搜索结果列表
     */
    List<SearchResultDTO> search(String keyword, int page, int pageSize);
    
    /**
     * 索引帖子
     * @param postId 帖子ID
     */
    void indexPost(Long postId);
    
    /**
     * 索引资源
     * @param resourceId 资源ID
     */
    void indexResource(Long resourceId);
    
    /**
     * 删除帖子索引
     * @param postId 帖子ID
     */
    void deletePostIndex(Long postId);
    
    /**
     * 删除资源索引
     * @param resourceId 资源ID
     */
    void deleteResourceIndex(Long resourceId);
    
    /**
     * 批量索引所有帖子
     * @return 索引的帖子数量
     */
    int indexAllPosts();
    
    /**
     * 批量索引所有资源
     * @return 索引的资源数量
     */
    int indexAllResources();
}

