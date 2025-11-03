package com.minecraftforum.controller;

import com.minecraftforum.common.Result;
import com.minecraftforum.entity.User;
import com.minecraftforum.service.UserService;
import com.minecraftforum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        // 清除密码信息
        user.setPassword(null);
        return Result.success(user);
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

