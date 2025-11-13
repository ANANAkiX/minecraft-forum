package com.minecraftforum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.dto.LoginRequest;
import com.minecraftforum.dto.RegisterRequest;
import com.minecraftforum.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User updateUser(User user);
    IPage<User> getUserList(Page<User> page, String keyword);
    User updateUserRole(Long userId, String role);
}

