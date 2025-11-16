package com.minecraftforum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.dto.LoginRequest;
import com.minecraftforum.dto.RegisterRequest;
import com.minecraftforum.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User updateUser(User user);
    IPage<User> getUserList(Page<User> page, String keyword);
    String uploadAvatar(MultipartFile file, Long userId);
    /**
     * 管理员创建用户
     * @param username 用户名
     * @param password 密码（可选，如果不提供则生成随机密码）
     * @param nickname 昵称
     * @param email 邮箱
     * @param status 状态（0-正常，1-禁用）
     * @return 创建的用户
     */
    User createUser(String username, String password, String nickname, String email, Integer status);
}