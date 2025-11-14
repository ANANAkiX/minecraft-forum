package com.minecraftforum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 论坛配置类
 * 从 application.yml 中读取论坛相关配置
 */
@Configuration
@ConfigurationProperties(prefix = "forum")
@Data
public class ForumConfig {
    
    /**
     * 开发环境模式：true-资源自动审核通过，false-资源需要审核
     */
    private Boolean devMode = true;
    
    /**
     * 是否允许匿名访问首页和论坛：true-允许未登录用户访问，false-需要登录
     */
    private Boolean anonymousAccess = true;
}
