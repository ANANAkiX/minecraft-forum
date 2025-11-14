package com.minecraftforum.service;

import com.minecraftforum.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    /**
     * 上传文件到 OSS
     * @param file 文件
     * @param resourceId 所属资源ID（可选）
     * @param userId 用户ID
     * @return 文件信息
     */
    SysFile uploadFile(MultipartFile file, Long resourceId, Long userId);
    
    /**
     * 根据ID获取文件信息
     * @param id 文件ID
     * @return 文件信息
     */
    SysFile getFileById(Long id);
    
    /**
     * 删除文件
     * @param id 文件ID
     */
    void deleteFile(Long id);
    
    /**
     * 根据资源ID获取文件列表
     * @param resourceId 资源ID
     * @return 文件列表
     */
    java.util.List<SysFile> getFilesByResourceId(Long resourceId);
    
    /**
     * 下载文件（从OSS获取文件流）
     * @param fileId 文件ID
     * @return 文件输入流和文件名
     */
    java.util.Map<String, Object> downloadFile(Long fileId);
    
    /**
     * 获取所有文件列表（分页）
     * @param page 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词（文件名）
     * @param resourceId 资源ID（可选，用于筛选）
     * @return 文件列表
     */
    com.baomidou.mybatisplus.core.metadata.IPage<SysFile> getFileList(com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysFile> page, String keyword, Long resourceId);
}

