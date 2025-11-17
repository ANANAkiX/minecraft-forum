package com.minecraftforum.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 权限缓存服务
 * 负责将权限数据缓存到Redis，并提供查询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private static final String REDIS_KEY_ALL_PERMISSIONS = "permission:all"; // 所有权限列表
    private static final String REDIS_KEY_PERMISSION_MAP = "permission:map:"; // 权限映射 key格式: permission:map:GET:/api/users
    private static final String REDIS_KEY_NO_PERMISSION = "permission:no:"; // 无权限配置标记 key格式: permission:no:GET:/api/users
    private static final long CACHE_EXPIRE_HOURS = 24; // 缓存过期时间：24小时
    private static final String NO_PERMISSION_MARKER = "__NO_PERMISSION__"; // 无权限配置标记值

    private final StringRedisTemplate redisTemplate;
    private final PermissionMapper permissionMapper;
    
    // 初始化 ObjectMapper，支持 Java 8 时间类型
    private final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * 初始化权限缓存（项目启动时调用）
     */
    public void initPermissionCache() {
        try {
            log.info("开始加载权限数据到Redis缓存...");
            List<Permission> allPermissions = permissionMapper.selectList(null);
            
            // 缓存所有权限列表
            String permissionsJson = objectMapper.writeValueAsString(allPermissions);
            redisTemplate.opsForValue().set(REDIS_KEY_ALL_PERMISSIONS, permissionsJson, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 构建权限映射表（method + apiurl -> permission code）
            Map<String, String> permissionMap = new HashMap<>();
            for (Permission permission : allPermissions) {
                if (permission.getStatus() != null && permission.getStatus() == 1 
                    && permission.getMethodtype() != null && permission.getApiurl() != null) {
                    String key = buildPermissionKey(permission.getMethodtype(), permission.getApiurl());
                    permissionMap.put(key, permission.getCode());
                }
            }
            
            // 缓存权限映射表
            for (Map.Entry<String, String> entry : permissionMap.entrySet()) {
                String redisKey = REDIS_KEY_PERMISSION_MAP + entry.getKey();
                redisTemplate.opsForValue().set(redisKey, entry.getValue(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }
            
            log.info("权限数据加载完成，共 {} 个权限，已缓存到Redis", allPermissions.size());
        } catch (Exception e) {
            log.error("初始化权限缓存失败", e);
        }
    }

    /**
     * 根据接口（method + url）查询权限代码
     * 优先从Redis查询，如果不存在则查询数据库并缓存
     */
    public String getPermissionCodeByApi(String method, String apiUrl) {
        if (method == null || apiUrl == null) {
            return null;
        }
        
        String key = buildPermissionKey(method, apiUrl);
        String redisKey = REDIS_KEY_PERMISSION_MAP + key;
        
        // 先从Redis查询
        try {
            String permissionCode = redisTemplate.opsForValue().get(redisKey);
            if (permissionCode != null && !permissionCode.isEmpty()) {
                return permissionCode;
            }
        } catch (Exception e) {
            log.warn("从Redis查询权限失败: {}", e.getMessage());
        }
        
        // Redis中没有，查询数据库（精确匹配）
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Permission> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(Permission::getMethodtype, method)
                   .eq(Permission::getApiurl, apiUrl)
                   .eq(Permission::getStatus, 1)
                   .last("LIMIT 1");
            
            Permission permission = permissionMapper.selectOne(wrapper);
            
            if (permission != null && permission.getCode() != null) {
                // 找到权限，缓存到Redis
                try {
                    redisTemplate.opsForValue().set(redisKey, permission.getCode(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.warn("缓存权限到Redis失败: {}", e.getMessage());
                }
                return permission.getCode();
            }
            // 精确匹配失败，不缓存"无权限"标记，因为可能还有模糊匹配的机会
        } catch (Exception e) {
            log.error("从数据库查询权限失败", e);
        }
        
        return null;
    }

    /**
     * 模糊匹配查询权限代码（支持路径参数）
     */
    public String getPermissionCodeByApiFuzzy(String method, String requestUrl) {
        if (method == null || requestUrl == null) {
            return null;
        }
        
        // 规范化URL
        requestUrl = normalizeUrl(requestUrl);
        
        // 先从Redis获取所有权限
        try {
            String permissionsJson = redisTemplate.opsForValue().get(REDIS_KEY_ALL_PERMISSIONS);
            if (permissionsJson != null && !permissionsJson.isEmpty()) {
                List<Permission> permissions = objectMapper.readValue(permissionsJson, new TypeReference<List<Permission>>() {});
                
                for (Permission permission : permissions) {
                    if (permission.getStatus() != null && permission.getStatus() == 1
                        && method.equals(permission.getMethodtype())
                        && permission.getApiurl() != null) {
                        String apiUrl = normalizeUrl(permission.getApiurl());
                        
                        // 精确匹配
                        if (requestUrl.equals(apiUrl)) {
                            return permission.getCode();
                        }
                        
                        // 模糊匹配（支持路径参数）
                        if (matchesUrl(requestUrl, apiUrl)) {
                            // 缓存到Redis
                            String key = buildPermissionKey(method, requestUrl);
                            String redisKey = REDIS_KEY_PERMISSION_MAP + key;
                            try {
                                redisTemplate.opsForValue().set(redisKey, permission.getCode(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                            } catch (Exception e) {
                                log.warn("缓存权限到Redis失败: {}", e.getMessage());
                            }
                            return permission.getCode();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从Redis查询权限列表失败: {}", e.getMessage());
        }
        
        // 检查是否已经标记为无权限配置
        String key = buildPermissionKey(method, requestUrl);
        String noPermissionKey = REDIS_KEY_NO_PERMISSION + key;
        try {
            String noPermissionMarker = redisTemplate.opsForValue().get(noPermissionKey);
            if (NO_PERMISSION_MARKER.equals(noPermissionMarker)) {
                // 已经确认无权限配置，直接返回null
                return null;
            }
        } catch (Exception e) {
            log.warn("从Redis查询无权限标记失败: {}", e.getMessage());
        }
        
        // Redis中没有或查询失败，从数据库查询
        try {
            List<Permission> allPermissions = permissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Permission>()
                    .eq(Permission::getMethodtype, method)
                    .eq(Permission::getStatus, 1)
                    .isNotNull(Permission::getApiurl)
            );
            
            for (Permission permission : allPermissions) {
                String apiUrl = normalizeUrl(permission.getApiurl());
                
                // 精确匹配
                if (requestUrl.equals(apiUrl)) {
                    // 缓存到Redis
                    String redisKey = REDIS_KEY_PERMISSION_MAP + key;
                    try {
                        redisTemplate.opsForValue().set(redisKey, permission.getCode(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.warn("缓存权限到Redis失败: {}", e.getMessage());
                    }
                    return permission.getCode();
                }
                
                // 模糊匹配（支持路径参数）
                if (matchesUrl(requestUrl, apiUrl)) {
                    // 缓存到Redis
                    String redisKey = REDIS_KEY_PERMISSION_MAP + key;
                    try {
                        redisTemplate.opsForValue().set(redisKey, permission.getCode(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                    } catch (Exception e) {
                        log.warn("缓存权限到Redis失败: {}", e.getMessage());
                    }
                    return permission.getCode();
                }
            }
            
            // 没有找到匹配的权限配置，缓存"无权限"标记
            try {
                redisTemplate.opsForValue().set(noPermissionKey, NO_PERMISSION_MARKER, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("缓存无权限标记到Redis失败: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("从数据库查询权限失败", e);
        }
        
        return null;
    }

    /**
     * 清除权限缓存
     */
    public void clearPermissionCache() {
        try {
            // 清除所有权限相关的缓存
            redisTemplate.delete(REDIS_KEY_ALL_PERMISSIONS);
            
            // 清除权限映射表（使用模式匹配）
            redisTemplate.delete(redisTemplate.keys(REDIS_KEY_PERMISSION_MAP + "*"));
            
            // 清除无权限标记（使用模式匹配）
            redisTemplate.delete(redisTemplate.keys(REDIS_KEY_NO_PERMISSION + "*"));
            
            log.info("权限缓存已清除");
        } catch (Exception e) {
            log.error("清除权限缓存失败", e);
        }
    }

    /**
     * 刷新权限缓存（重新加载）
     */
    public void refreshPermissionCache() {
        clearPermissionCache();
        initPermissionCache();
    }

    /**
     * 构建权限缓存key
     */
    private String buildPermissionKey(String method, String apiUrl) {
        return method + ":" + normalizeUrl(apiUrl);
    }

    /**
     * 匹配 URL（支持路径参数）
     * 例如：/api/admin/users/6/roles 匹配 /api/admin/users/{id}/roles
     */
    private boolean matchesUrl(String requestUrl, String apiUrl) {
        if (requestUrl == null || apiUrl == null) {
            return false;
        }
        
        // 规范化两个 URL
        requestUrl = normalizeUrl(requestUrl);
        apiUrl = normalizeUrl(apiUrl);
        
        // 如果完全相等，直接返回 true
        if (requestUrl.equals(apiUrl)) {
            return true;
        }
        
        // 如果 apiUrl 不包含 {，则不需要模糊匹配
        if (!apiUrl.contains("{")) {
            return false;
        }
        
        // 将 apiUrl 中的 {xxx} 替换为正则表达式 [^/]+（匹配除斜杠外的任意字符）
        // 例如：/api/admin/users/{id}/roles -> /api/admin/users/[^/]+/roles
        String pattern = apiUrl.replaceAll("\\{[^}]+\\}", "[^/]+");
        // 转义斜杠并添加开始和结束锚点
        pattern = "^" + pattern.replace("/", "\\/") + "$";
        
        boolean matches = requestUrl.matches(pattern);
        if (matches) {
            log.debug("URL匹配成功: requestUrl={}, apiUrl={}, pattern={}", requestUrl, apiUrl, pattern);
        }
        return matches;
    }

    /**
     * 规范化 URL
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "/";
        }
        // 移除多余的斜杠
        url = url.replaceAll("/+", "/");
        // 确保以/开头
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        // 移除末尾的斜杠（除非是根路径）
        if (url.length() > 1 && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}

