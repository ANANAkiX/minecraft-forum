package com.minecraftforum.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.config.OssConfig;
import com.minecraftforum.entity.SysFile;
import com.minecraftforum.mapper.SysFileMapper;
import com.minecraftforum.service.FileService;
import com.minecraftforum.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final OssConfig ossConfig;
    private final SysFileMapper sysFileMapper;
    private final SnowflakeIdGenerator idGenerator = SnowflakeIdGenerator.getInstance();
    
    @Override
    public SysFile uploadFile(MultipartFile file, Long resourceId, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 获取文件扩展名
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        // 使用雪花算法生成唯一文件名
        String uniqueFileName = idGenerator.nextId() + extension;
        
        // 构建 OSS 对象键（路径）
        String objectKey = "files/" + uniqueFileName;
        
        OSS ossClient = null;
        InputStream inputStream = null;
        
        try {
            // 创建 OSS 客户端
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            
            // 上传文件
            inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectKey,
                    inputStream
            );
            ossClient.putObject(putObjectRequest);
            
            // 构建文件访问 URL
            String domain = ossConfig.getDomain();
            String fileUrl;
            if (domain != null && domain.endsWith("/")) {
                fileUrl = domain + objectKey;
            } else if (domain != null) {
                fileUrl = domain + "/" + objectKey;
            } else {
                // 如果没有配置域名，使用 endpoint 和 bucket 构建
                String endpoint = ossConfig.getEndpoint().replace("https://", "").replace("http://", "");
                fileUrl = "https://" + ossConfig.getBucketName() + "." + endpoint + "/" + objectKey;
            }
            
            // 保存文件信息到数据库
            SysFile sysFile = new SysFile();
            sysFile.setResourceId(resourceId);
            sysFile.setOriginalName(originalFilename);
            sysFile.setFileName(uniqueFileName);
            sysFile.setFileUrl(fileUrl);
            sysFile.setFileSize(file.getSize());
            sysFile.setFileType(file.getContentType());
            sysFile.setCreateUser(userId);
            sysFile.setUpdateUser(userId);
            sysFile.setCreateTime(LocalDateTime.now());
            sysFile.setUpdateTime(LocalDateTime.now());
            
            sysFileMapper.insert(sysFile);
            
            log.info("文件上传成功: originalName={}, fileName={}, fileUrl={}", 
                    originalFilename, uniqueFileName, fileUrl);
            
            return sysFile;
            
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } finally {
            // 关闭流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.error("关闭输入流失败", e);
                }
            }
            // 关闭 OSS 客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public SysFile getFileById(Long id) {
        return sysFileMapper.selectById(id);
    }
    
    @Override
    public List<SysFile> getFilesByResourceId(Long resourceId) {
        if (resourceId == null) {
            return new java.util.ArrayList<>();
        }
        return sysFileMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getResourceId, resourceId)
                .orderByDesc(SysFile::getCreateTime)
        );
    }
    
    @Override
    public Map<String, Object> downloadFile(Long fileId) {
        SysFile sysFile = sysFileMapper.selectById(fileId);
        if (sysFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        
        OSS ossClient = null;
        InputStream inputStream = null;
        
        try {
            // 创建 OSS 客户端
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            
            // 构建 OSS 对象键（路径）
            String objectKey = "files/" + sysFile.getFileName();
            
            // 从 OSS 获取文件流
            inputStream = ossClient.getObject(ossConfig.getBucketName(), objectKey).getObjectContent();
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("inputStream", inputStream);
            result.put("fileName", sysFile.getOriginalName());
            result.put("fileSize", sysFile.getFileSize());
            result.put("contentType", sysFile.getFileType());
            
            // 注意：这里返回的 inputStream 需要调用者负责关闭
            // 但为了保持 OSS 客户端的生命周期，我们将 OSS 客户端也放入结果中
            result.put("ossClient", ossClient);
            
            return result;
            
        } catch (Exception e) {
            // 如果出错，关闭已创建的流和客户端
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    log.error("关闭输入流失败", ex);
                }
            }
            if (ossClient != null) {
                ossClient.shutdown();
            }
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(Long id) {
        SysFile sysFile = sysFileMapper.selectById(id);
        if (sysFile == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        
        OSS ossClient = null;
        try {
            // 创建 OSS 客户端
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            
            // 从 OSS 删除文件
            String objectKey = "files/" + sysFile.getFileName();
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            
            // 从数据库删除记录
            sysFileMapper.deleteById(id);
            
            log.info("文件删除成功: id={}, fileName={}", id, sysFile.getFileName());
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public IPage<SysFile> getFileList(Page<SysFile> page, String keyword, Long resourceId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（文件名）
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysFile::getOriginalName, keyword);
        }
        
        // 资源ID筛选
        if (resourceId != null) {
            wrapper.eq(SysFile::getResourceId, resourceId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(SysFile::getCreateTime);
        
        return sysFileMapper.selectPage(page, wrapper);
    }
}

