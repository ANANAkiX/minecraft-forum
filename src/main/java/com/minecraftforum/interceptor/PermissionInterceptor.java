package com.minecraftforum.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.common.Result;
import com.minecraftforum.service.PermissionCacheService;
import com.minecraftforum.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 权限拦截器
 * 从Redis缓存查询权限并验证用户是否有权限访问
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final SecurityUtil securityUtil;
    private final PermissionCacheService permissionCacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求方法和 URL
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        
        // 移除查询参数
        if (requestURI.contains("?")) {
            requestURI = requestURI.substring(0, requestURI.indexOf("?"));
        }
        
        // 规范化 URL（移除多余的斜杠）
        requestURI = normalizeUrl(requestURI);
        
        // 从Redis缓存查询该接口对应的权限代码（优先）
        String permissionCode = permissionCacheService.getPermissionCodeByApi(method, requestURI);
        
        // 如果精确匹配失败，尝试模糊匹配（支持路径参数，如 /api/forum/posts/{id}）
        if (permissionCode == null) {
            permissionCode = permissionCacheService.getPermissionCodeByApiFuzzy(method, requestURI);
        }
        
        // 如果缓存和数据库都没有配置该接口的权限，则允许访问（向后兼容）
        if (!StringUtils.hasText(permissionCode)) {
            return true;
        }
        
        // 检查用户是否有该权限
        if (!securityUtil.hasPermission(permissionCode)) {
            // 没有权限，返回 403
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            
            Result<Object> result = Result.error(403, "无权限访问");
            String json = objectMapper.writeValueAsString(result);
            
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
            
            log.warn("用户无权限访问接口: {} {}, 需要权限: {}", method, requestURI, permissionCode);
            return false;
        }
        
        return true;
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

