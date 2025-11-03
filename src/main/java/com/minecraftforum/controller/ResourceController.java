package com.minecraftforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.common.Result;
import com.minecraftforum.config.AnonymousAccess;
import com.minecraftforum.dto.ResourceDTO;
import com.minecraftforum.entity.Resource;
import com.minecraftforum.entity.ResourceTag;
import com.minecraftforum.mapper.ResourceTagMapper;
import com.minecraftforum.service.ResourceService;
import com.minecraftforum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final JwtUtil jwtUtil;
    private final ResourceTagMapper resourceTagMapper;
    private final ObjectMapper objectMapper;

    @GetMapping("/list")
    @AnonymousAccess
    public Result<Map<String, Object>> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
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

    @GetMapping("/{id}")
    public Result<ResourceDTO> getResourceById(@PathVariable Long id) {
        ResourceDTO resource = resourceService.getResourceById(id);
        return Result.success(resource);
    }

    @PostMapping
    public Result<ResourceDTO> createResource(@RequestParam("title") String title,
                                              @RequestParam("description") String description,
                                              @RequestParam("content") String content,
                                              @RequestParam("category") String category,
                                              @RequestParam("version") String version,
                                              @RequestParam(value = "tags", required = false) String tags,
                                              @RequestParam(value = "file", required = false) MultipartFile file,
                                              HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setDescription(description);
        resource.setContent(content);
        resource.setCategory(category);
        resource.setVersion(version);
        resource.setAuthorId(userId);

        // TODO: 处理文件上传
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

    @PutMapping("/{id}")
    public Result<ResourceDTO> updateResource(@PathVariable Long id, @RequestBody Resource resource,
                                              HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        ResourceDTO existing = resourceService.getResourceById(id);
        if (existing == null || !existing.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限修改此资源");
        }

        resource.setId(id);
        resourceService.updateResource(resource);
        ResourceDTO updated = resourceService.getResourceById(id);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        ResourceDTO resource = resourceService.getResourceById(id);
        if (resource == null || !resource.getAuthorId().equals(userId)) {
            return Result.error(403, "没有权限删除此资源");
        }

        resourceService.deleteResource(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeResource(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        resourceService.likeResource(id, userId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeResource(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        resourceService.unlikeResource(id, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteResource(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        resourceService.favoriteResource(id, userId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteResource(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        resourceService.unfavoriteResource(id, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/download")
    public Result<Void> downloadResource(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        resourceService.downloadResource(id, userId);
        return Result.success(null);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

