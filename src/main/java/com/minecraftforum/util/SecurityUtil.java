package com.minecraftforum.util;

import com.minecraftforum.entity.User;
import com.minecraftforum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 * 提供获取当前用户、Token等常用方法
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {
    
    private final TokenUtil tokenUtil;
    private final UserService userService;
    
    /**
     * 从请求中获取Token
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 从当前请求中获取Token
     */
    public String getTokenFromCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return getTokenFromRequest(attributes.getRequest());
        }
        return null;
    }
    
    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            return user != null ? user.getId() : null;
        }
        
        // 如果SecurityContext中没有，尝试从Token获取
        String token = getTokenFromCurrentRequest();
        if (token != null && tokenUtil.isTokenValid(token)) {
            return tokenUtil.getUserIdFromToken(token);
        }
        
        return null;
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            return userService.getUserById(userId);
        }
        return null;
    }
    
    /**
     * 获取当前登录用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        
        String token = getTokenFromCurrentRequest();
        if (token != null && tokenUtil.isTokenValid(token)) {
            return tokenUtil.getUsernameFromToken(token);
        }
        
        return null;
    }
    
    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(permission));
        }
        return false;
    }
    
    /**
     * 检查当前用户是否有指定角色
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
        }
        return false;
    }
}







