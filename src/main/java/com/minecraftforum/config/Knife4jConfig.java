package com.minecraftforum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minecraft 论坛 API 文档")
                        .version("1.0.0")
                        .description("Minecraft 论坛系统 API 接口文档")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}










