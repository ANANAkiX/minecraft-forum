package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.config.ForumConfig;
import com.minecraftforum.dto.ResourceDTO;
import com.minecraftforum.entity.*;
import com.minecraftforum.mapper.*;
import com.minecraftforum.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    
    private final ResourceMapper resourceMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;
    private final DownloadLogMapper downloadLogMapper;
    private final UserMapper userMapper;
    private final ResourceTagMapper resourceTagMapper;
    private final ForumConfig forumConfig;
    
    @Override
    public IPage<ResourceDTO> getResourceList(Page<Resource> page, String category, String keyword, Long authorId) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(Resource::getCategory, category);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Resource::getTitle, keyword)
                    .or().like(Resource::getDescription, keyword));
        }
        
        if (authorId != null) {
            wrapper.eq(Resource::getAuthorId, authorId);
        }
        
        // 如果没有指定作者ID，只显示已审核的资源
        // 如果指定了作者ID，显示该作者的所有资源（包括待审核的）
        if (authorId == null) {
            wrapper.eq(Resource::getStatus, "APPROVED");
        } else {
            wrapper.in(Resource::getStatus, "APPROVED", "PENDING");
        }
        
        wrapper.orderByDesc(Resource::getCreateTime);
        
        IPage<Resource> resourcePage = resourceMapper.selectPage(page, wrapper);
        
        // 转换为 DTO 并填充作者信息
        IPage<ResourceDTO> dtoPage = new Page<>(resourcePage.getCurrent(), resourcePage.getSize(), resourcePage.getTotal());
        List<ResourceDTO> dtoList = resourcePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    @Override
    public ResourceDTO getResourceById(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            return null;
        }
        return convertToDTO(resource);
    }
    
    private ResourceDTO convertToDTO(Resource resource) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setDescription(resource.getDescription());
        dto.setContent(resource.getContent());
        dto.setCategory(resource.getCategory());
        dto.setVersion(resource.getVersion());
        dto.setAuthorId(resource.getAuthorId());
        dto.setFileUrl(resource.getFileUrl());
        dto.setThumbnailUrl(resource.getThumbnailUrl());
        dto.setDownloadCount(resource.getDownloadCount());
        dto.setLikeCount(resource.getLikeCount());
        dto.setFavoriteCount(resource.getFavoriteCount());
        dto.setStatus(resource.getStatus());
        dto.setCreateTime(resource.getCreateTime());
        dto.setUpdateTime(resource.getUpdateTime());
        
        // 查询作者信息
        User author = userMapper.selectById(resource.getAuthorId());
        if (author != null) {
            dto.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            dto.setAuthorAvatar(author.getAvatar());
        }
        
        // 查询标签
        LambdaQueryWrapper<ResourceTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ResourceTag::getResourceId, resource.getId());
        List<ResourceTag> tags = resourceTagMapper.selectList(tagWrapper);
        dto.setTags(tags.stream().map(ResourceTag::getTagName).collect(Collectors.toList()));
        
        return dto;
    }
    
    @Override
    public Resource createResource(Resource resource) {
        resource.setDownloadCount(0);
        resource.setLikeCount(0);
        resource.setFavoriteCount(0);
        // 根据配置决定资源状态：开发模式自动审核通过，否则需要审核
        resource.setStatus(forumConfig.getDevMode() ? "APPROVED" : "PENDING");
        resource.setCreateTime(LocalDateTime.now());
        resource.setUpdateTime(LocalDateTime.now());
        resourceMapper.insert(resource);
        return resource;
    }
    
    @Override
    public Resource updateResource(Resource resource) {
        resource.setUpdateTime(LocalDateTime.now());
        resourceMapper.updateById(resource);
        return resource;
    }
    
    @Override
    public void deleteResource(Long id) {
        resourceMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void likeResource(Long resourceId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getResourceId, resourceId);
        wrapper.eq(Like::getUserId, userId);
        
        if (likeMapper.selectOne(wrapper) == null) {
            Like like = new Like();
            like.setUserId(userId);
            like.setResourceId(resourceId);
            like.setCreateTime(LocalDateTime.now());
            likeMapper.insert(like);
            
            Resource resource = resourceMapper.selectById(resourceId);
            if (resource != null) {
                resource.setLikeCount(resource.getLikeCount() + 1);
                resourceMapper.updateById(resource);
            }
        }
    }
    
    @Override
    @Transactional
    public void unlikeResource(Long resourceId, Long userId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getResourceId, resourceId);
        wrapper.eq(Like::getUserId, userId);
        
        Like existingLike = likeMapper.selectOne(wrapper);
        if (existingLike != null) {
            likeMapper.delete(wrapper);
            
            Resource resource = resourceMapper.selectById(resourceId);
            if (resource != null) {
                resource.setLikeCount(Math.max(0, resource.getLikeCount() - 1));
                resourceMapper.updateById(resource);
            }
        }
    }
    
    @Override
    @Transactional
    public void favoriteResource(Long resourceId, Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getResourceId, resourceId);
        wrapper.eq(Favorite::getUserId, userId);
        
        if (favoriteMapper.selectOne(wrapper) == null) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setResourceId(resourceId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            
            Resource resource = resourceMapper.selectById(resourceId);
            if (resource != null) {
                resource.setFavoriteCount(resource.getFavoriteCount() + 1);
                resourceMapper.updateById(resource);
            }
        }
    }
    
    @Override
    @Transactional
    public void unfavoriteResource(Long resourceId, Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getResourceId, resourceId);
        wrapper.eq(Favorite::getUserId, userId);
        
        Favorite existingFavorite = favoriteMapper.selectOne(wrapper);
        if (existingFavorite != null) {
            favoriteMapper.delete(wrapper);
            
            Resource resource = resourceMapper.selectById(resourceId);
            if (resource != null) {
                resource.setFavoriteCount(Math.max(0, resource.getFavoriteCount() - 1));
                resourceMapper.updateById(resource);
            }
        }
    }
    
    @Override
    @Transactional
    public void downloadResource(Long resourceId, Long userId) {
        DownloadLog log = new DownloadLog();
        log.setUserId(userId);
        log.setResourceId(resourceId);
        log.setCreateTime(LocalDateTime.now());
        downloadLogMapper.insert(log);
        
        Resource resource = resourceMapper.selectById(resourceId);
        if (resource != null) {
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            resourceMapper.updateById(resource);
        }
    }
}

