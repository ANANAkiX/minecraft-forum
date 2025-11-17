package com.minecraftforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.common.Result;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import com.minecraftforum.dto.ResourceDTO;
import com.minecraftforum.entity.Resource;
import com.minecraftforum.entity.ResourceTag;
import com.minecraftforum.mapper.ResourceTagMapper;
import com.minecraftforum.service.ResourceService;
import com.minecraftforum.util.SecurityUtil;
import com.minecraftforum.dto.UpdateResourceRequest;
import com.minecraftforum.dto.DeleteRequest;
import com.minecraftforum.dto.ActionRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源控制器
 * 处理资源相关的CRUD操作、点赞、收藏、下载等
 */
@Tag(name = "资源管理", description = "资源相关的增删改查、点赞、收藏、下载等接口")
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceTagMapper resourceTagMapper;
    private final ObjectMapper objectMapper;
    private final SecurityUtil securityUtil;

    /**
     * 获取资源列表
     */
    @Operation(summary = "获取资源列表", description = "分页获取资源列表，支持按分类、关键词、作者筛选")
    @GetMapping("/list")
    @AnonymousAccess
    public Result<Map<String, Object>> getResourceList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "分类代码")
            @RequestParam(required = false) String category,
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "作者ID")
            @RequestParam(required = false) Long authorId) {

        Page<Resource> pageObj = new Page<>(page, pageSize);
        IPage<ResourceDTO> result = resourceService.getResourceList(pageObj, category, keyword, authorId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        return Result.success(data);
    }

    /**
     * 获取资源详情
     */
    @Operation(summary = "获取资源详情", description = "根据ID获取资源的详细信息")
    @GetMapping("/detail")
    @AnonymousAccess
    public Result<ResourceDTO> getResourceById(
            @Parameter(description = "资源ID", required = true)
            @RequestParam Long id) {
        if (id == null) {
            return Result.error(400, "资源ID不能为空");
        }
        ResourceDTO resource = resourceService.getResourceById(id);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        return Result.success(resource);
    }

    /**
     * 创建资源
     */
    @Operation(summary = "创建资源", description = "创建新的资源，需要resource:create权限")
    @PostMapping
    public Result<ResourceDTO> createResource(
            @Parameter(description = "资源标题", required = true)
            @RequestParam("title") String title,
            @Parameter(description = "资源描述", required = true)
            @RequestParam("description") String description,
            @Parameter(description = "资源内容", required = true)
            @RequestParam("content") String content,
            @Parameter(description = "资源分类", required = true)
            @RequestParam("category") String category,
            @Parameter(description = "资源版本", required = true)
            @RequestParam("version") String version,
            @Parameter(description = "资源标签（JSON数组或逗号分隔）")
            @RequestParam(value = "tags", required = false) String tags) {
        
        Long userId = securityUtil.getCurrentUserId();
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setContent(content);
        resource.setCategory(category);
        resource.setVersion(version);
        resource.setAuthorId(userId);
        
        // 注意：不再设置 fileUrl，文件通过 sys_file 表关联

        // 创建资源
        Resource created = resourceService.createResource(resource);

        // 解析并保存标签
        if (tags != null && !tags.isEmpty()) {
            try {
                List<String> tagList;
                if (tags.startsWith("[")) {
                    // JSON 格式的标签数组
                    tagList = objectMapper.readValue(tags, List.class);
                } else {
                    // 逗号分隔的标签字符串
                    tagList = new ArrayList<>();
                    String[] tagArray = tags.split(",");
                    for (String tag : tagArray) {
                        if (!tag.trim().isEmpty()) {
                            tagList.add(tag.trim());
                        }
                    }
                }

                // 保存标签
                for (String tagName : tagList) {
                    ResourceTag tagEntity = new ResourceTag();
                    tagEntity.setResourceId(created.getId());
                    tagEntity.setTagName(tagName);
                    resourceTagMapper.insert(tagEntity);
                }
            } catch (Exception e) {
                // 标签解析失败，忽略继续
            }
        }

        // 返回包含完整信息的 DTO
        ResourceDTO dto = resourceService.getResourceById(created.getId());
        return Result.success(dto);
    }

    /**
     * 更新资源
     */
    @Operation(summary = "更新资源", description = "更新资源信息，只能更新自己创建的资源")
    @PutMapping
    public Result<ResourceDTO> updateResource(
            @Parameter(description = "资源信息", required = true)
            @RequestBody UpdateResourceRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        Long userId = securityUtil.getCurrentUserId();
        ResourceDTO existing = resourceService.getResourceById(request.getId());
        if (existing == null) {
            return Result.error(404, "资源不存在");
        }
        if (!existing.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限修改此资源");
        }

        Resource resource = new Resource();
        resource.setId(request.getId());
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setContent(request.getContent());
        resource.setCategory(request.getCategory());
        resource.setVersion(request.getVersion());
        
        resourceService.updateResource(resource);
        
        // 更新标签（如果提供了标签）
        if (request.getTags() != null) {
            // 删除旧标签
            LambdaQueryWrapper<ResourceTag> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ResourceTag::getResourceId, request.getId());
            resourceTagMapper.delete(deleteWrapper);
            
            // 解析并保存新标签
            String tags = request.getTags();
            if (!tags.isEmpty()) {
                try {
                    List<String> tagList;
                    if (tags.startsWith("[")) {
                        // JSON 格式的标签数组
                        tagList = objectMapper.readValue(tags, List.class);
                    } else {
                        // 逗号分隔的标签字符串
                        tagList = new ArrayList<>();
                        String[] tagArray = tags.split(",");
                        for (String tag : tagArray) {
                            if (!tag.trim().isEmpty()) {
                                tagList.add(tag.trim());
                            }
                        }
                    }
                    
                    // 保存新标签
                    for (String tagName : tagList) {
                        ResourceTag tagEntity = new ResourceTag();
                        tagEntity.setResourceId(request.getId());
                        tagEntity.setTagName(tagName);
                        resourceTagMapper.insert(tagEntity);
                    }
                } catch (Exception e) {
                    // 标签解析失败，忽略继续
                }
            }
        }
        
        ResourceDTO updated = resourceService.getResourceById(request.getId());
        return Result.success(updated);
    }

    /**
     * 删除资源
     */
    @Operation(summary = "删除资源", description = "删除资源，只能删除自己创建的资源")
    @DeleteMapping
    public Result<Void> deleteResource(
            @RequestBody DeleteRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        Long userId = securityUtil.getCurrentUserId();
        ResourceDTO resource = resourceService.getResourceById(request.getId());
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        if (!resource.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限删除此资源");
        }

        resourceService.deleteResource(request.getId());
        return Result.success(null);
    }

    /**
     * 点赞资源
     */
    @Operation(summary = "点赞资源", description = "为资源点赞")
    @PostMapping("/like")
    public Result<Void> likeResource(
            @RequestBody ActionRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        Long userId = securityUtil.getCurrentUserId();
        resourceService.likeResource(request.getId(), userId);
        return Result.success(null);
    }

    /**
     * 取消点赞资源
     */
    @Operation(summary = "取消点赞", description = "取消对资源的点赞")
    @DeleteMapping("/like")
    public Result<Void> unlikeResource(
            @RequestBody ActionRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        Long userId = securityUtil.getCurrentUserId();
        resourceService.unlikeResource(request.getId(), userId);
        return Result.success(null);
    }

    /**
     * 收藏资源
     */
    @Operation(summary = "收藏资源", description = "收藏资源到个人收藏夹")
    @PostMapping("/favorite")
    public Result<Void> favoriteResource(
            @RequestBody ActionRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        Long userId = securityUtil.getCurrentUserId();
        resourceService.favoriteResource(request.getId(), userId);
        return Result.success(null);
    }

    /**
     * 取消收藏资源
     */
    @Operation(summary = "取消收藏", description = "从个人收藏夹中移除资源")
    @DeleteMapping("/favorite")
    public Result<Void> unfavoriteResource(
            @RequestBody ActionRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        Long userId = securityUtil.getCurrentUserId();
        resourceService.unfavoriteResource(request.getId(), userId);
        return Result.success(null);
    }

    /**
     * 下载资源
     */
    @Operation(summary = "下载资源", description = "下载资源文件，记录下载日志，需要resource:download权限")
    @PostMapping("/download")
    public Result<Void> downloadResource(
            @RequestBody ActionRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        Long userId = securityUtil.getCurrentUserId();
        resourceService.downloadResource(request.getId(), userId);
        return Result.success(null);
    }
}
