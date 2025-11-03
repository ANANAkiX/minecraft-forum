package com.minecraftforum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.minecraftforum.mapper")
@EnableConfigurationProperties
public class MinecraftForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinecraftForumApplication.class, args);
    }
}

