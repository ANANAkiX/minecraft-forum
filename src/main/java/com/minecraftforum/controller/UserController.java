package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.User;
import com.minecraftforum.service.PermissionService;
import com.minecraftforum.service.UserService;
import com.minecraftforum.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 处理用户信息查询、更新等操作
 */
@Tag(name = "用户管理", description = "用户信息查询、更新等接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    
    private final UserService userService;
    private final PermissionService permissionService;
    private final SecurityUtil securityUtil;
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息，包括权限列表")
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        // 清除密码信息，确保不会返回给前端
        user.clearPassword();
        
        // 获取用户的所有权限
        List<Permission> permissions = permissionService.getUserPermissions(userId);
        List<String> permissionCodes = permissions.stream()
            .map(Permission::getCode)
            .collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("status", user.getStatus());
        result.put("createTime", user.getCreateTime());
        result.put("updateTime", user.getUpdateTime());
        result.put("permissions", permissionCodes);
        
        return Result.success(result);
    }
    
    /**
     * 更新当前用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的昵称、邮箱等信息")
    @PutMapping("/info")
    public Result<User> updateUserInfo(
            @Parameter(description = "用户信息", required = true)
            @RequestBody User user) {
        Long userId = securityUtil.getCurrentUserId();
        User existingUser = userService.getUserById(userId);
        if (existingUser == null) {
            return Result.error(404, "用户不存在");
        }
        
        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        
        User updatedUser = userService.updateUser(existingUser);
        // 清除密码信息，确保不会返回给前端
        updatedUser.clearPassword();
        return Result.success(updatedUser);
    }
    
    /**
     * 上传用户头像
     */
    @Operation(summary = "上传头像", description = "上传用户头像文件到OSS")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @Parameter(description = "头像文件", required = true)
            @RequestParam("file") MultipartFile file) {
        Long userId = securityUtil.getCurrentUserId();
        // 验证文件
        if (file == null || file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只能上传图片文件");
        }
        
        // 验证文件大小（2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error(400, "图片大小不能超过 2MB");
        }
        
        try {
            String avatarUrl = userService.uploadAvatar(file, userId);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error(500, "头像上传失败: " + e.getMessage());
        }
    }
}
