package com.minecraftforum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.dto.ResourceDTO;
import com.minecraftforum.entity.Resource;

public interface ResourceService {
    IPage<ResourceDTO> getResourceList(Page<Resource> page, String category, String keyword, Long authorId);
    ResourceDTO getResourceById(Long id);
    Resource createResource(Resource resource);
    Resource updateResource(Resource resource);
    void deleteResource(Long id);
    void likeResource(Long resourceId, Long userId);
    void unlikeResource(Long resourceId, Long userId);
    void favoriteResource(Long resourceId, Long userId);
    void unfavoriteResource(Long resourceId, Long userId);
    void downloadResource(Long resourceId, Long userId);
}

