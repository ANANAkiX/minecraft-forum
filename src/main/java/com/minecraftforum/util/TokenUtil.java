package com.minecraftforum.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.config.TokenConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 工具类
 * 使用 UUID 作为 Token，数据存储在 Redis 中
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUtil {
    
    private static final String TOKEN_PREFIX = "token:";
    private static final String USER_TOKEN_PREFIX = "user:token:"; // 用于存储用户的所有Token（多点登录）
    
    private final StringRedisTemplate redisTemplate;
    private final TokenConfig tokenConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 生成 Token（UUID）
     * @param userId 用户ID
     * @param username 用户名
     * @param permissions 权限列表
     * @return UUID Token
     */
    public String generateToken(Long userId, String username, List<String> permissions) {
        // 生成 UUID
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 构建 Token 数据
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("userId", userId);
        tokenData.put("username", username);
        tokenData.put("permissions", permissions);
        tokenData.put("createTime", System.currentTimeMillis());
        
        try {
            // 存储到 Redis
            String tokenKey = TOKEN_PREFIX + token;
            String tokenJson = objectMapper.writeValueAsString(tokenData);
            long expirationSeconds = tokenConfig.getExpiration() / 1000;
            redisTemplate.opsForValue().set(tokenKey, tokenJson, expirationSeconds, TimeUnit.SECONDS);
            
            // 如果允许多点登录，将 Token 添加到用户的 Token 列表中
            if (tokenConfig.getAllowMultipleLogin() != null && tokenConfig.getAllowMultipleLogin()) {
                String userTokenKey = USER_TOKEN_PREFIX + userId;
                redisTemplate.opsForSet().add(userTokenKey, token);
                redisTemplate.expire(userTokenKey, expirationSeconds, TimeUnit.SECONDS);
            } else {
                // 如果不允许多点登录，删除该用户的所有旧 Token
                String userTokenKey = USER_TOKEN_PREFIX + userId;
                List<String> oldTokens = redisTemplate.opsForSet().members(userTokenKey)
                    .stream()
                    .map(String::valueOf)
                    .toList();
                
                if (oldTokens != null && !oldTokens.isEmpty()) {
                    for (String oldToken : oldTokens) {
                        redisTemplate.delete(TOKEN_PREFIX + oldToken);
                    }
                    redisTemplate.delete(userTokenKey);
                }
                
                // 添加新 Token
                redisTemplate.opsForSet().add(userTokenKey, token);
                redisTemplate.expire(userTokenKey, expirationSeconds, TimeUnit.SECONDS);
            }
            
            log.debug("生成 Token: userId={}, username={}, token={}", userId, username, token);
            return token;
        } catch (Exception e) {
            log.error("生成 Token 失败", e);
            throw new RuntimeException("生成 Token 失败", e);
        }
    }
    
    /**
     * 从 Token 获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            String tokenKey = TOKEN_PREFIX + token;
            String tokenJson = redisTemplate.opsForValue().get(tokenKey);
            if (tokenJson == null || tokenJson.isEmpty()) {
                return null;
            }
            
            Map<String, Object> tokenData = objectMapper.readValue(tokenJson, new TypeReference<Map<String, Object>>() {});
            Object userIdObj = tokenData.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            return null;
        } catch (Exception e) {
            log.error("从 Token 获取用户ID失败", e);
            return null;
        }
    }
    
    /**
     * 从 Token 获取用户名
     */
    public String getUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            String tokenKey = TOKEN_PREFIX + token;
            String tokenJson = redisTemplate.opsForValue().get(tokenKey);
            if (tokenJson == null || tokenJson.isEmpty()) {
                return null;
            }
            
            Map<String, Object> tokenData = objectMapper.readValue(tokenJson, new TypeReference<Map<String, Object>>() {});
            Object usernameObj = tokenData.get("username");
            return usernameObj != null ? usernameObj.toString() : null;
        } catch (Exception e) {
            log.error("从 Token 获取用户名失败", e);
            return null;
        }
    }
    
    /**
     * 从 Token 获取权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return List.of();
        }
        
        try {
            String tokenKey = TOKEN_PREFIX + token;
            String tokenJson = redisTemplate.opsForValue().get(tokenKey);
            if (tokenJson == null || tokenJson.isEmpty()) {
                return List.of();
            }
            
            Map<String, Object> tokenData = objectMapper.readValue(tokenJson, new TypeReference<Map<String, Object>>() {});
            Object permissionsObj = tokenData.get("permissions");
            if (permissionsObj instanceof List) {
                return (List<String>) permissionsObj;
            }
            return List.of();
        } catch (Exception e) {
            log.error("从 Token 获取权限列表失败", e);
            return List.of();
        }
    }
    
    /**
     * 检查 Token 是否有效
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        try {
            String tokenKey = TOKEN_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
        } catch (Exception e) {
            log.error("检查 Token 有效性失败", e);
            return false;
        }
    }
    
    /**
     * 删除 Token
     */
    public void deleteToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        try {
            String tokenKey = TOKEN_PREFIX + token;
            redisTemplate.delete(tokenKey);
            
            // 从用户的 Token 列表中移除
            Long userId = getUserIdFromToken(token);
            if (userId != null) {
                String userTokenKey = USER_TOKEN_PREFIX + userId;
                redisTemplate.opsForSet().remove(userTokenKey, token);
            }
        } catch (Exception e) {
            log.error("删除 Token 失败", e);
        }
    }
    
    /**
     * 更新 Token 中的权限列表（不更新 UUID）
     */
    public void updateTokenPermissions(String token, List<String> permissions) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        try {
            String tokenKey = TOKEN_PREFIX + token;
            String tokenJson = redisTemplate.opsForValue().get(tokenKey);
            if (tokenJson == null || tokenJson.isEmpty()) {
                return;
            }
            
            Map<String, Object> tokenData = objectMapper.readValue(tokenJson, new TypeReference<Map<String, Object>>() {});
            tokenData.put("permissions", permissions);
            
            String updatedTokenJson = objectMapper.writeValueAsString(tokenData);
            
            // 获取剩余过期时间
            Long expire = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
            if (expire != null && expire > 0) {
                redisTemplate.opsForValue().set(tokenKey, updatedTokenJson, expire, TimeUnit.SECONDS);
                log.debug("更新 Token 权限: token={}, permissions={}", token, permissions);
            }
        } catch (Exception e) {
            log.error("更新 Token 权限失败", e);
        }
    }
    
    /**
     * 更新用户所有 Token 的权限
     */
    public void updateUserAllTokensPermissions(Long userId, List<String> permissions) {
        try {
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            List<String> tokens = redisTemplate.opsForSet().members(userTokenKey)
                .stream()
                .map(String::valueOf)
                .toList();
            
            if (tokens != null && !tokens.isEmpty()) {
                for (String token : tokens) {
                    updateTokenPermissions(token, permissions);
                }
                log.info("更新用户所有 Token 权限: userId={}, tokenCount={}", userId, tokens.size());
            }
        } catch (Exception e) {
            log.error("更新用户所有 Token 权限失败", e);
        }
    }
    
    /**
     * 删除用户的所有 Token（登出所有设备）
     */
    public void deleteUserAllTokens(Long userId) {
        try {
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            List<String> tokens = redisTemplate.opsForSet().members(userTokenKey)
                .stream()
                .map(String::valueOf)
                .toList();
            
            if (tokens != null && !tokens.isEmpty()) {
                for (String token : tokens) {
                    redisTemplate.delete(TOKEN_PREFIX + token);
                }
            }
            redisTemplate.delete(userTokenKey);
            log.info("删除用户所有 Token: userId={}, tokenCount={}", userId, tokens != null ? tokens.size() : 0);
        } catch (Exception e) {
            log.error("删除用户所有 Token 失败", e);
        }
    }
}


