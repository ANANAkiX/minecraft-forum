package com.minecraftforum.security;

import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import com.minecraftforum.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenUtil tokenUtil;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 检查接口是否有 @AnonymousAccess 注解
        boolean isAnonymousAccess = false;
        HandlerExecutionChain handler;
        try {
            handler = handlerMapping.getHandler(request);
            if (handler != null && handler.getHandler() instanceof HandlerMethod handlerMethod) {
                // 检查接口或类上是否有 @AnonymousAccess 注解
                isAnonymousAccess = handlerMethod.hasMethodAnnotation(AnonymousAccess.class)
                        || handlerMethod.getBeanType().isAnnotationPresent(AnonymousAccess.class);
            }
        } catch (Exception ignore) {
        }

        // 无论接口是否允许匿名访问，都尝试解析Token并设置认证信息
        // 这样即使接口允许匿名访问，如果用户提供了有效的token，也能正确获取用户权限信息
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && tokenUtil.isTokenValid(token)) {
            // 有token，尝试解析并设置认证信息
            try {
                String username = tokenUtil.getUsernameFromToken(token);
                Long userId = tokenUtil.getUserIdFromToken(token);

                if (username != null && userId != null) {
                    // 构建权限列表
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    
                    // 从 Redis 中提取权限列表
                    List<String> permissions = tokenUtil.getPermissionsFromToken(token);
                    for (String permissionCode : permissions) {
                        // 将权限代码添加为GrantedAuthority
                        authorities.add(new SimpleGrantedAuthority(permissionCode));
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token 无效，继续过滤链（不清除已有的认证信息）
            }
        }

        // 如果接口不允许匿名访问且没有有效的认证信息，Spring Security会在后续的过滤器链中处理
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

