package com.minecraftforum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Token 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class TokenConfig {
    /**
     * JWT 密钥（保留用于向后兼容）
     */
    private String secret;
    
    /**
     * Token 过期时间（毫秒）
     */
    private Long expiration;
    
    /**
     * 是否允许多点登录
     * true: 允许多个客户端同时登录
     * false: 只允许一个客户端（新登录会踢掉旧登录）
     */
    private Boolean allowMultipleLogin = true;
}


