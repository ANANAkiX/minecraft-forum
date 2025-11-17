package com.minecraftforum.util;

import com.aliyun.oss.OSS;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.InputStream;

/**
 * 资源工具类
 * 提供资源关闭等常用方法
 */
@Slf4j
public class ResourceUtil {
    
    /**
     * 安全关闭输入流
     */
    public static void closeQuietly(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                log.warn("关闭输入流失败", e);
            }
        }
    }
    
    /**
     * 安全关闭 OSS 客户端
     */
    public static void shutdownQuietly(OSS ossClient) {
        if (ossClient != null) {
            try {
                ossClient.shutdown();
            } catch (Exception e) {
                log.warn("关闭 OSS 客户端失败", e);
            }
        }
    }
    
    /**
     * 安全关闭 Closeable 资源
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("关闭资源失败", e);
            }
        }
    }
    
    /**
     * 同时关闭输入流和 OSS 客户端
     */
    public static void closeResources(InputStream inputStream, OSS ossClient) {
        closeQuietly(inputStream);
        shutdownQuietly(ossClient);
    }
}

