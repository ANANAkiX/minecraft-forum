package com.minecraftforum.service;

import com.minecraftforum.dto.LoginRequest;
import com.minecraftforum.dto.RegisterRequest;
import com.minecraftforum.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User updateUser(User user);
}

