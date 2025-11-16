package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.dto.SearchResultDTO;
import com.minecraftforum.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 * 权限检查由 PermissionInterceptor 统一处理
 */
@Tag(name = "搜索管理", description = "Elasticsearch 搜索接口")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    /**
     * 搜索帖子和资源
     * 需要 elasticsearch:search 权限（由 PermissionInterceptor 统一检查）
     */
    @Operation(summary = "搜索", description = "搜索帖子和资源，需要 elasticsearch:search 权限")
    @GetMapping
    public Result<List<SearchResultDTO>> search(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "页码，从1开始", required = false)
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", required = false)
            @RequestParam(defaultValue = "10") int pageSize) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error(400, "搜索关键词不能为空");
        }
        
        // 检查搜索服务是否可用
        if (!searchService.isSearchAvailable()) {
            return Result.error(503, "搜索服务暂时不可用，Elasticsearch 正在连接中，请稍后重试");
        }
        
        try {
            List<SearchResultDTO> results = searchService.search(keyword.trim(), page, pageSize);
            return Result.success(results);
        } catch (Exception e) {
            // 记录错误日志
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Elasticsearch 服务暂时不可用")) {
                // Elasticsearch 不可用，返回 503 错误
                return Result.error(503, errorMessage);
            }
            // 其他错误，记录日志并返回空结果
            org.slf4j.LoggerFactory.getLogger(SearchController.class)
                    .error("搜索失败: keyword={}", keyword, e);
            return Result.error(500, "搜索失败，请稍后重试");
        }
    }
    
    /**
     * 批量索引所有帖子
     * 需要 admin:search:manage 权限
     */
    @Operation(summary = "批量索引帖子", description = "将数据库中所有帖子索引到Elasticsearch，需要admin:search:manage权限")
    @PostMapping("/index/posts")
    public Result<Integer> indexAllPosts() {
        int count = searchService.indexAllPosts();
        return Result.success(count);
    }
    
    /**
     * 批量索引所有资源
     * 需要 admin:search:manage 权限
     */
    @Operation(summary = "批量索引资源", description = "将数据库中所有资源索引到Elasticsearch，需要admin:search:manage权限")
    @PostMapping("/index/resources")
    public Result<Integer> indexAllResources() {
        int count = searchService.indexAllResources();
        return Result.success(count);
    }
}

