package com.minecraftforum.security;

import com.minecraftforum.config.AnonymousUrlCollector;
import com.minecraftforum.config.CorsConfig;
import com.minecraftforum.config.ForumConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ForumConfig forumConfig;
    private final CorsConfig corsConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> anonymousUrls = new ArrayList<>(AnonymousUrlCollector.getAnonymousUrls());
        
        // 根据配置决定是否允许首页和论坛的GET请求匿名访问
        boolean allowAnonymousAccess = forumConfig.getAnonymousAccess() != null && forumConfig.getAnonymousAccess();
        
        // 根据配置过滤匿名访问URL列表
        // 如果允许匿名访问，保留所有URL；否则，移除首页和论坛相关的URL
        List<String> filteredUrls = filterAnonymousUrls(anonymousUrls, allowAnonymousAccess);
        
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            // 动态放行@AnonymousAccess注解的接口（根据配置过滤）
                            .requestMatchers(filteredUrls.toArray(String[]::new)).permitAll()
                            // 认证相关接口（登录、注册）允许匿名访问，但刷新Token需要认证
                            .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                            // API文档相关接口允许匿名访问
                            .requestMatchers("/doc.html",
                                    "/webjars/**",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**",
                                    "/favicon.ico")
                            .permitAll()
                            // 配置接口允许匿名访问（前端需要获取配置信息）
                            .requestMatchers("/api/config/**").permitAll();
                    
                    auth
                            // 后台管理接口：需要page:admin权限或ROLE_ADMIN角色（兼容旧系统）
                            .requestMatchers("/api/admin/**")
                            .hasAnyAuthority("page:admin", "ROLE_ADMIN")
                            // 其他所有接口都需要认证（包括/api/auth/refresh）
                            .anyRequest()
                            .authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    /**
     * 根据配置过滤匿名访问URL列表
     * 如果允许匿名访问，返回所有URL；否则，移除首页和论坛相关的URL
     */
    private List<String> filterAnonymousUrls(List<String> urls, boolean allowAnonymousAccess) {
        if (allowAnonymousAccess) {
            return urls;
        }
        // 如果不允许匿名访问，移除首页和论坛相关的URL
        // 这样这些URL就不会被permitAll()，会走后面的authenticated()检查
        return urls.stream()
                .filter(url -> !url.contains("/api/resource/list") 
                        && !url.contains("/api/resource/") && !url.equals("/api/resource")
                        && !url.contains("/api/forum/posts")
                        && !url.contains("/api/category-config/enabled"))
                .toList();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 从配置中读取允许的源
        if (corsConfig.getAllowedOrigins() != null && !corsConfig.getAllowedOrigins().isEmpty()) {
            configuration.setAllowedOrigins(corsConfig.getAllowedOrigins());
        } else {
            // 默认值
            configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        }
        
        // 从配置中读取允许的方法
        if (corsConfig.getAllowedMethods() != null && !corsConfig.getAllowedMethods().isEmpty()) {
            configuration.setAllowedMethods(corsConfig.getAllowedMethods());
        } else {
            // 默认值
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }
        
        // 从配置中读取允许的请求头
        if (corsConfig.getAllowedHeaders() != null && !corsConfig.getAllowedHeaders().isEmpty()) {
            configuration.setAllowedHeaders(corsConfig.getAllowedHeaders());
        } else {
            // 默认值
            configuration.setAllowedHeaders(Arrays.asList("*"));
        }
        
        // 从配置中读取是否允许凭证
        configuration.setAllowCredentials(corsConfig.getAllowCredentials() != null ? corsConfig.getAllowCredentials() : true);
        
        // 从配置中读取预检请求缓存时间
        configuration.setMaxAge(corsConfig.getMaxAge() != null ? corsConfig.getMaxAge() : 3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

