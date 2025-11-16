package com.minecraftforum.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.common.Result;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.mapper.PermissionMapper;
import com.minecraftforum.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.List;

/**
 * 权限拦截器
 * 动态从数据库查询权限并验证用户是否有权限访问
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionMapper permissionMapper;
    private final SecurityUtil securityUtil;
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
        
        // 从数据库查询该接口对应的权限代码
        // 先精确匹配
        LambdaQueryWrapper<Permission> exactWrapper = new LambdaQueryWrapper<>();
        exactWrapper.eq(Permission::getMethodtype, method)
                   .eq(Permission::getApiurl, requestURI)
                   .eq(Permission::getStatus, 1)
                   .last("LIMIT 1");
        
        Permission permission = permissionMapper.selectOne(exactWrapper);
        
        // 如果精确匹配失败，尝试模糊匹配（支持路径参数，如 /api/forum/posts/{id}）
        if (permission == null) {
            List<Permission> allPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                    .eq(Permission::getMethodtype, method)
                    .eq(Permission::getStatus, 1)
                    .isNotNull(Permission::getApiurl)
            );
            
            for (Permission p : allPermissions) {
                String apiUrl = p.getApiurl();
                if (apiUrl != null && matchesUrl(requestURI, apiUrl)) {
                    permission = p;
                    break;
                }
            }
        }
        
        // 如果数据库中没有配置该接口的权限，则允许访问（向后兼容）
        if (permission == null || !StringUtils.hasText(permission.getCode())) {
            return true;
        }
        
        // 检查用户是否有该权限
        String permissionCode = permission.getCode();
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
     * 匹配 URL（支持路径参数）
     * 例如：/api/forum/posts/123 匹配 /api/forum/posts/{id}
     */
    private boolean matchesUrl(String requestUrl, String apiUrl) {
        if (requestUrl == null || apiUrl == null) {
            return false;
        }
        
        // 规范化两个 URL
        requestUrl = normalizeUrl(requestUrl);
        apiUrl = normalizeUrl(apiUrl);
        
        // 如果完全相等，直接返回 true
        if (requestUrl.equals(apiUrl)) {
            return true;
        }
        
        // 如果 apiUrl 不包含 {，则不需要模糊匹配
        if (!apiUrl.contains("{")) {
            return false;
        }
        
        // 将 apiUrl 中的 {xxx} 替换为正则表达式
        String pattern = apiUrl.replaceAll("\\{[^}]+\\}", "[^/]+");
        pattern = "^" + pattern.replace("/", "\\/") + "$";
        
        return requestUrl.matches(pattern);
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

