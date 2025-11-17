package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import com.minecraftforum.entity.SysFile;
import com.minecraftforum.service.FileService;
import com.minecraftforum.dto.DeleteRequest;
import com.minecraftforum.util.ResourceUtil;
import com.minecraftforum.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理", description = "文件上传、下载等接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {
    
    private final FileService fileService;
    private final SecurityUtil securityUtil;
    
    /**
     * 上传单个文件
     */
    @Operation(summary = "上传文件", description = "上传文件到阿里云 OSS，支持 multipart/form-data")
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(
            @Parameter(description = "文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "所属资源ID（可选）")
            @RequestParam(value = "resourceId", required = false) Long resourceId) {
        
        // 获取当前用户ID
        Long userId = securityUtil.getCurrentUserId();
        try {
            // 上传文件
            SysFile sysFile = fileService.uploadFile(file, resourceId, userId);
            
            // 构建返回结果（符合富文本编辑器要求的格式）
            Map<String, Object> result = new HashMap<>();
            result.put("url", sysFile.getFileUrl());
            result.put("name", sysFile.getOriginalName());
            result.put("type", sysFile.getFileType());
            result.put("size", sysFile.getFileSize());
            
            return Result.success(result);
            
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传文件
     */
    @Operation(summary = "批量上传文件", description = "批量上传多个文件到阿里云 OSS，支持 multipart/form-data")
    @PostMapping("/upload/batch")
    public Result<List<Map<String, Object>>> uploadFiles(
            @Parameter(description = "文件列表", required = true)
            @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "所属资源ID（可选）")
            @RequestParam(value = "resourceId", required = false) Long resourceId) {
        
        // 获取当前用户ID
        Long userId = securityUtil.getCurrentUserId();
        if (files == null || files.length == 0) {
            return Result.error(400, "请至少选择一个文件");
        }
        
        try {
            List<Map<String, Object>> results = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                
                // 上传文件
                SysFile sysFile = fileService.uploadFile(file, resourceId, userId);
                
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("url", sysFile.getFileUrl());
                result.put("name", sysFile.getOriginalName());
                result.put("type", sysFile.getFileType());
                result.put("size", sysFile.getFileSize());
                result.put("id", sysFile.getId());
                
                results.add(result);
            }
            
            return Result.success(results);
            
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件信息
     */
    @Operation(summary = "获取文件信息", description = "根据文件ID获取文件信息")
    @GetMapping("/detail")
    public Result<SysFile> getFile(
            @Parameter(description = "文件ID", required = true)
            @RequestParam Long id) {
        
        if (id == null) {
            return Result.error(400, "文件ID不能为空");
        }
        
        SysFile sysFile = fileService.getFileById(id);
        if (sysFile == null) {
            return Result.error(404, "文件不存在");
        }
        
        return Result.success(sysFile);
    }
    
    /**
     * 根据资源ID获取文件列表
     * 注意：此接口允许匿名访问，但会根据用户权限决定是否返回敏感信息（文件URL和文件名）
     */
    @Operation(summary = "获取资源文件列表", description = "根据资源ID获取该资源关联的所有文件。无下载权限的用户将无法看到文件URL和文件名")
    @GetMapping("/resource")
    @AnonymousAccess
    public Result<List<SysFile>> getFilesByResourceId(
            @Parameter(description = "资源ID", required = true)
            @RequestParam Long resourceId) {
        
        if (resourceId == null) {
            return Result.error(400, "资源ID不能为空");
        }
        
        List<SysFile> files = fileService.getFilesByResourceId(resourceId);
        
        // 根据用户权限决定是否返回敏感信息
        // 注意：这是业务逻辑层面的权限控制，不是访问控制
        // PermissionInterceptor 只控制接口访问权限，这里控制返回数据的详细程度
        if (!securityUtil.hasPermission("resource:download")) {
            // 如果没有下载权限，屏蔽敏感信息（文件URL和文件名）
            files.forEach(file -> {
                file.setFileUrl(null);
                file.setFileName(null);
            });
        }
        
        return Result.success(files);
    }
    
    /**
     * 下载文件
     * 权限检查由 PermissionInterceptor 统一处理
     */
    @Operation(summary = "下载文件", description = "从OSS下载文件，需要resource:download权限（由PermissionInterceptor统一检查）")
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "文件ID", required = true)
            @RequestParam Long id) {
        
        if (id == null) {
            return ResponseEntity.status(400).build();
        }
        
        com.aliyun.oss.OSS ossClient = null;
        InputStream inputStream = null;
        
        try {
            // 从OSS获取文件流
            Map<String, Object> fileData = fileService.downloadFile(id);
            inputStream = (InputStream) fileData.get("inputStream");
            String fileName = (String) fileData.get("fileName");
            Long fileSize = (Long) fileData.get("fileSize");
            String contentType = (String) fileData.get("contentType");
            ossClient = (com.aliyun.oss.OSS) fileData.get("ossClient");
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"));
            headers.setContentLength(fileSize);
            
            // 对文件名进行URL编码，支持中文文件名
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", encodedFileName);
            
            // 创建响应实体，在关闭时关闭OSS客户端
            final com.aliyun.oss.OSS finalOssClient = ossClient;
            final InputStream finalInputStream = inputStream;
            
            // 创建自定义的 InputStreamResource，实现 Closeable 接口
            InputStreamResource resource = new InputStreamResource(inputStream) {
                // 实现 Closeable 接口的 close 方法（不添加 @Override，因为父类可能没有此方法）
                public void close() throws java.io.IOException {
                    try {
                        // 关闭输入流
                        if (finalInputStream != null) {
                            finalInputStream.close();
                        }
                    } finally {
                        // 关闭输入流时，也关闭OSS客户端
                        if (finalOssClient != null) {
                            finalOssClient.shutdown();
                        }
                    }
                }
            };
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            // 关闭已创建的流和客户端
            ResourceUtil.closeResources(inputStream, ossClient);
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            // 关闭已创建的流和客户端
            ResourceUtil.closeResources(inputStream, ossClient);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "删除文件（从 OSS 和数据库中删除）")
    @DeleteMapping
    public Result<Void> deleteFile(
            @RequestBody DeleteRequest request) {
        
        if (request.getId() == null) {
            return Result.error(400, "文件ID不能为空");
        }

        try {
            fileService.deleteFile(request.getId());
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "文件删除失败: " + e.getMessage());
        }
    }
}

