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
    @GetMapping("/{id}")
    @AnonymousAccess
    public Result<ResourceDTO> getResourceById(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id) {
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
    @PutMapping("/{id}")
    public Result<ResourceDTO> updateResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "资源信息", required = true)
            @RequestBody Resource resource) {
        
        Long userId = securityUtil.getCurrentUserId();
        ResourceDTO existing = resourceService.getResourceById(id);
        if (existing == null) {
            return Result.error(404, "资源不存在");
        }
        if (!existing.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限修改此资源");
        }

        resource.setId(id);
        resourceService.updateResource(resource);
        ResourceDTO updated = resourceService.getResourceById(id);
        return Result.success(updated);
    }

    /**
     * 删除资源
     */
    @Operation(summary = "删除资源", description = "删除资源，只能删除自己创建的资源")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        ResourceDTO resource = resourceService.getResourceById(id);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        if (!resource.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限删除此资源");
        }

        resourceService.deleteResource(id);
        return Result.success(null);
    }

    /**
     * 点赞资源
     */
    @Operation(summary = "点赞资源", description = "为资源点赞")
    @PostMapping("/{id}/like")
    public Result<Void> likeResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        resourceService.likeResource(id, userId);
        return Result.success(null);
    }

    /**
     * 取消点赞资源
     */
    @Operation(summary = "取消点赞", description = "取消对资源的点赞")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        resourceService.unlikeResource(id, userId);
        return Result.success(null);
    }

    /**
     * 收藏资源
     */
    @Operation(summary = "收藏资源", description = "收藏资源到个人收藏夹")
    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        resourceService.favoriteResource(id, userId);
        return Result.success(null);
    }

    /**
     * 取消收藏资源
     */
    @Operation(summary = "取消收藏", description = "从个人收藏夹中移除资源")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        resourceService.unfavoriteResource(id, userId);
        return Result.success(null);
    }

    /**
     * 下载资源
     */
    @Operation(summary = "下载资源", description = "下载资源文件，记录下载日志，需要resource:download权限")
    @PostMapping("/{id}/download")
    public Result<Void> downloadResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id) {
        
        Long userId = securityUtil.getCurrentUserId();
        resourceService.downloadResource(id, userId);
        return Result.success(null);
    }
}
