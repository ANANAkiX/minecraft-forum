package com.minecraftforum.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.config.OssConfig;
import com.minecraftforum.dto.LoginRequest;
import com.minecraftforum.dto.RegisterRequest;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.User;
import com.minecraftforum.mapper.UserMapper;
import com.minecraftforum.service.PermissionService;
import com.minecraftforum.service.UserService;
import com.minecraftforum.util.JwtUtil;
import com.minecraftforum.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OssConfig ossConfig;
    private final SnowflakeIdGenerator idGenerator = SnowflakeIdGenerator.getInstance();
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    
    @Override
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        user.setStatus(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public String login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getStatus() != 0) {
            throw new RuntimeException("账号已被禁用");
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
        
        // 生成包含权限的Token
        return jwtUtil.generateTokenWithPermissions(user.getId(), user.getUsername(), user.getRole(), permissionCodes);
    }
    
    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
    
    @Override
    public User updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }
    
    @Override
    public IPage<User> getUserList(Page<User> page, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 支持关键词搜索（用户名、昵称、邮箱）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);
        
        // 查询所有用户（包括已删除的，如果表有deleted字段，MyBatis Plus会自动处理）
        IPage<User> result = userMapper.selectPage(page, wrapper);
        
        // 清除密码信息
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return result;
    }
    
    @Override
    public User updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }
    
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 获取文件扩展名
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        // 使用雪花算法生成唯一文件名
        String uniqueFileName = idGenerator.nextId() + extension;
        
        // 构建 OSS 对象键（路径）- 头像存储在 avatar 目录
        String objectKey = "avatar/" + uniqueFileName;
        
        OSS ossClient = null;
        InputStream inputStream = null;
        
        try {
            // 创建 OSS 客户端
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            
            // 上传文件
            inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectKey,
                    inputStream
            );
            ossClient.putObject(putObjectRequest);
            
            // 构建文件访问 URL
            String domain = ossConfig.getDomain();
            String avatarUrl;
            if (domain != null && domain.endsWith("/")) {
                avatarUrl = domain + objectKey;
            } else if (domain != null) {
                avatarUrl = domain + "/" + objectKey;
            } else {
                // 如果没有配置域名，使用 endpoint 和 bucket 构建
                String endpoint = ossConfig.getEndpoint().replace("https://", "").replace("http://", "");
                avatarUrl = "https://" + ossConfig.getBucketName() + "." + endpoint + "/" + objectKey;
            }
            
            // 更新用户头像URL
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setAvatar(avatarUrl);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
            
            log.info("头像上传成功: userId={}, avatarUrl={}", userId, avatarUrl);
            
            return avatarUrl;
            
        } catch (Exception e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("头像上传失败: " + e.getMessage(), e);
        } finally {
            // 关闭流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.error("关闭输入流失败", e);
                }
            }
            // 关闭 OSS 客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}

