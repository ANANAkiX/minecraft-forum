package com.minecraftforum.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.common.Result;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import com.minecraftforum.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 登录拦截器
 * 统一处理未登录判断，兼容 AnonymousAccess 注解
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理 HandlerMethod（Controller 方法）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查是否有 @AnonymousAccess 注解（方法级别或类级别）
        boolean isAnonymousAccess = handlerMethod.hasMethodAnnotation(AnonymousAccess.class)
                || handlerMethod.getBeanType().isAnnotationPresent(AnonymousAccess.class);
        
        // 如果允许匿名访问，直接放行
        if (isAnonymousAccess) {
            return true;
        }
        
        // 检查是否登录
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            // 未登录，返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            
            Result<Object> result = Result.error(401, "未登录");
            String json = objectMapper.writeValueAsString(result);
            
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
            
            log.warn("未登录用户尝试访问需要认证的接口: {} {}", request.getMethod(), request.getRequestURI());
            return false;
        }
        
        return true;
    }
}

