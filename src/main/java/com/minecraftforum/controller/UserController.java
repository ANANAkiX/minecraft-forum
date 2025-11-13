package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.User;
import com.minecraftforum.service.PermissionService;
import com.minecraftforum.service.UserService;
import com.minecraftforum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        // 清除密码信息
        user.setPassword(null);
        
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
        result.put("role", user.getRole());
        result.put("status", user.getStatus());
        result.put("createTime", user.getCreateTime());
        result.put("updateTime", user.getUpdateTime());
        result.put("permissions", permissionCodes);
        
        return Result.success(result);
    }
    
    @PutMapping("/info")
    public Result<User> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        User existingUser = userService.getUserById(userId);
        existingUser.setNickname(user.getNickname());
        existingUser.setEmail(user.getEmail());
        
        User updatedUser = userService.updateUser(existingUser);
        updatedUser.setPassword(null);
        return Result.success(updatedUser);
    }
    
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                      HttpServletRequest request) {
        // TODO: 实现头像上传逻辑
        String token = getTokenFromRequest(request);
        // 返回头像URL
        return Result.success("/uploads/avatar/default.png");
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

