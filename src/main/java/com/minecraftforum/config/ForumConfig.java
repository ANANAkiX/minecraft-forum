package com.minecraftforum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "forum")
public class ForumConfig {
    /**
     * 是否开启开发环境模式
     * true: 资源自动审核通过（APPROVED）
     * false: 资源需要审核（PENDING）
     */
    private Boolean devMode = false;
}

