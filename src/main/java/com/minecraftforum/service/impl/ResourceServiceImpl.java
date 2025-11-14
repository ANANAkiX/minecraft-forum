package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.config.ForumConfig;
import com.minecraftforum.dto.ResourceDTO;
import com.minecraftforum.entity.*;
import com.minecraftforum.mapper.*;
import com.minecraftforum.service.ResourceService;
import com.minecraftforum.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final SecurityUtil securityUtil;
    
    @Override
    public IPage<ResourceDTO> getResourceList(Page<Resource> page, String category, String keyword, Long authorId) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(category)) {
            // 指定了具体分类，直接查询该分类
            wrapper.eq(Resource::getCategory, category);
        } else {
            // 当category为空（即"全部"）时，根据用户权限过滤分类
            // 如果允许匿名访问且用户未登录，显示所有分类
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAnonymous = authentication == null 
                    || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal());
            
            if (forumConfig.getAnonymousAccess() != null && forumConfig.getAnonymousAccess() && isAnonymous) {
                // 允许匿名访问且用户未登录，不进行权限过滤，显示所有分类
                // 不添加分类条件，查询所有分类的资源
            } else {
                // 根据用户权限过滤分类
                // 例如：用户有MOD和整合包权限，查询"全部"时只返回这两个分类下的文章
                // 如果用户没有MOD权限，查询"全部"时会排除MOD分类下的所有文章
                List<String> allowedCategories = getAllowedCategories();
                if (allowedCategories.isEmpty()) {
                    // 如果用户没有任何分类权限，返回空结果
                    IPage<ResourceDTO> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
                    emptyPage.setRecords(new ArrayList<>());
                    return emptyPage;
                }
                // 使用IN查询，只返回用户有权限的分类下的文章
                wrapper.in(Resource::getCategory, allowedCategories);
            }
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
        Long currentUserId = securityUtil.getCurrentUserId();
        IPage<ResourceDTO> dtoPage = new Page<>(resourcePage.getCurrent(), resourcePage.getSize(), resourcePage.getTotal());
        List<ResourceDTO> dtoList = resourcePage.getRecords().stream()
                .map(resource -> convertToDTO(resource, currentUserId))
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    @Override
    public IPage<ResourceDTO> getAllResourceList(Page<Resource> page, String category, String keyword, Long authorId) {
        return getAllResourceList(page, category, keyword, authorId, null);
    }
    
    public IPage<ResourceDTO> getAllResourceList(Page<Resource> page, String category, String keyword, Long authorId, String status) {
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
        
        // 如果指定了状态，则按状态筛选；否则显示所有状态的资源
        if (StringUtils.hasText(status)) {
            wrapper.eq(Resource::getStatus, status);
        }
        
        wrapper.orderByDesc(Resource::getCreateTime);
        
        IPage<Resource> resourcePage = resourceMapper.selectPage(page, wrapper);
        
        // 转换为 DTO 并填充作者信息
        Long currentUserId = securityUtil.getCurrentUserId();
        IPage<ResourceDTO> dtoPage = new Page<>(resourcePage.getCurrent(), resourcePage.getSize(), resourcePage.getTotal());
        List<ResourceDTO> dtoList = resourcePage.getRecords().stream()
                .map(resource -> convertToDTO(resource, currentUserId))
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 根据用户权限获取允许的分类列表
     * 
     * 权限映射关系：
     * - page:home:all -> 显示所有分类（PACK、MOD、RESOURCE）
     * - page:home:pack -> PACK（整合包）
     * - page:home:mod -> MOD
     * - page:home:resource -> RESOURCE（资源包）
     * 
     * 示例：
     * - 用户有MOD和整合包权限 -> 返回["PACK", "MOD"]，查询"全部"时只返回这两个分类下的文章
     * - 用户没有MOD权限 -> 返回["PACK"]，查询"全部"时会排除MOD分类下的所有文章
     * 
     * @return 用户有权限的分类代码列表
     */
    private List<String> getAllowedCategories() {
        List<String> allowedCategories = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 如果用户未登录或者是匿名用户，返回空列表（不允许查看任何分类）
        if (authentication == null 
                || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return allowedCategories;
        }
        
        // 检查用户权限
        boolean hasAll = securityUtil.hasPermission("page:home:all");
        boolean hasPack = securityUtil.hasPermission("page:home:pack");
        boolean hasMod = securityUtil.hasPermission("page:home:mod");
        boolean hasResource = securityUtil.hasPermission("page:home:resource");
        
        // 根据权限添加允许的分类
        if (hasAll) {
            // 如果有"全部"权限，显示所有可能的分类
            allowedCategories.add("PACK");      // 整合包
            allowedCategories.add("MOD");        // MOD
            allowedCategories.add("RESOURCE");   // 资源包
        } else {
            // 如果没有"全部"权限，只添加有权限的特定分类
            // 这样查询"全部"时，只会返回用户有权限的分类下的文章
            if (hasPack) {
                allowedCategories.add("PACK");  // 整合包
            }
            if (hasMod) {
                allowedCategories.add("MOD");    // MOD
            }
            if (hasResource) {
                allowedCategories.add("RESOURCE"); // 资源包
            }
        }
        
        return allowedCategories;
    }
    
    @Override
    public ResourceDTO getResourceById(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            return null;
        }
        return convertToDTO(resource, securityUtil.getCurrentUserId());
    }
    
    private ResourceDTO convertToDTO(Resource resource) {
        return convertToDTO(resource, null);
    }
    
    private ResourceDTO convertToDTO(Resource resource, Long currentUserId) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setDescription(resource.getDescription());
        dto.setContent(resource.getContent());
        dto.setCategory(resource.getCategory());
        dto.setVersion(resource.getVersion());
        dto.setAuthorId(resource.getAuthorId());
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
        
        // 如果用户已登录，检查是否已点赞和收藏
        if (currentUserId != null) {
            // 检查是否已点赞
            LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(Like::getResourceId, resource.getId());
            likeWrapper.eq(Like::getUserId, currentUserId);
            dto.setIsLiked(likeMapper.selectOne(likeWrapper) != null);
            
            // 检查是否已收藏
            LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(Favorite::getResourceId, resource.getId());
            favoriteWrapper.eq(Favorite::getUserId, currentUserId);
            dto.setIsFavorited(favoriteMapper.selectOne(favoriteWrapper) != null);
        } else {
            dto.setIsLiked(false);
            dto.setIsFavorited(false);
        }
        
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

