package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.config.custom.annotations.AnonymousAccess;
import com.minecraftforum.dto.LoginRequest;
import com.minecraftforum.dto.RegisterRequest;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.User;
import com.minecraftforum.service.PermissionService;
import com.minecraftforum.service.UserService;
import com.minecraftforum.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 处理用户登录、注册等认证相关操作
 */
@Tag(name = "认证管理", description = "用户登录、注册等认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    
    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "新用户注册接口，注册成功后自动登录并返回Token")
    @PostMapping("/register")
    @AnonymousAccess
    public Result<Map<String, Object>> register(
            @Parameter(description = "注册请求信息", required = true)
            @Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        
        // 获取用户的所有权限（新注册用户可能没有权限，但为了统一处理，仍然获取）
        List<Permission> permissions = permissionService.getUserPermissions(user.getId());
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
        
        // 兼容旧的角色判断：如果是ADMIN角色，添加page:admin权限
        if ("ADMIN".equals(user.getRole()) && !permissionCodes.contains("page:admin")) {
            permissionCodes.add("page:admin");
        }
        
        // 生成包含权限的Token
        String token = jwtUtil.generateTokenWithPermissions(user.getId(), user.getUsername(), user.getRole(), permissionCodes);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success(data);
    }
    
    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户登录接口，登录成功后返回Token和用户信息")
    @PostMapping("/login")
    @AnonymousAccess
    public Result<Map<String, Object>> login(
            @Parameter(description = "登录请求信息", required = true)
            @Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        User user = userService.getUserByUsername(request.getUsername());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success(data);
    }
    
    /**
     * 刷新Token（获取最新权限）
     */
    @Operation(summary = "刷新Token", description = "刷新当前用户的Token，获取最新的权限信息")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken() {
        // 从SecurityContext获取当前用户信息
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        if (user.getStatus() != 0) {
            return Result.error(403, "账号已被禁用");
        }
        
        // 获取用户的所有权限
        List<Permission> permissions = permissionService.getUserPermissions(user.getId());
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
        
        // 兼容旧的角色判断：如果是ADMIN角色，添加page:admin权限
        if ("ADMIN".equals(user.getRole()) && !permissionCodes.contains("page:admin")) {
            permissionCodes.add("page:admin");
        }
        
        // 生成包含最新权限的Token
        String newToken = jwtUtil.generateTokenWithPermissions(user.getId(), user.getUsername(), user.getRole(), permissionCodes);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        data.put("user", user);
        
        return Result.success(data);
    }
}
