package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.RolePermission;
import com.minecraftforum.entity.UserPermission;
import com.minecraftforum.entity.UserRole;
import com.minecraftforum.mapper.PermissionMapper;
import com.minecraftforum.mapper.RolePermissionMapper;
import com.minecraftforum.mapper.UserPermissionMapper;
import com.minecraftforum.mapper.UserRoleMapper;
import com.minecraftforum.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionMapper permissionMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    
    @Override
    public List<Permission> getAllPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectList(wrapper);
    }
    
    @Override
    public List<Permission> getEnabledPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1);
        wrapper.orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectList(wrapper);
    }
    
    @Override
    public List<Permission> getPermissionsByType(String type) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getType, type);
        wrapper.eq(Permission::getStatus, 1);
        wrapper.orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectList(wrapper);
    }
    
    @Override
    public Permission getPermissionById(Long id) {
        return permissionMapper.selectById(id);
    }
    
    @Override
    public Permission getPermissionByCode(String code) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getCode, code);
        return permissionMapper.selectOne(wrapper);
    }
    
    @Override
    public Permission createPermission(Permission permission) {
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        permissionMapper.insert(permission);
        return permission;
    }
    
    @Override
    public Permission updatePermission(Permission permission) {
        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.updateById(permission);
        return permission;
    }
    
    @Override
    public void deletePermission(Long id) {
        permissionMapper.deleteById(id);
    }
    
    @Override
    public List<Permission> getUserPermissions(Long userId) {
        // 获取用户直接分配的权限
        LambdaQueryWrapper<UserPermission> userPermWrapper = new LambdaQueryWrapper<>();
        userPermWrapper.eq(UserPermission::getUserId, userId);
        List<UserPermission> userPermissions = userPermissionMapper.selectList(userPermWrapper);
        Set<Long> permissionIds = userPermissions.stream()
            .map(UserPermission::getPermissionId)
            .collect(Collectors.toSet());
        
        // 获取用户通过角色获得的权限
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        
        List<String> rolePermissionCodes = userRoles.stream()
            .flatMap(ur -> {
                LambdaQueryWrapper<RolePermission> rolePermWrapper = new LambdaQueryWrapper<>();
                rolePermWrapper.eq(RolePermission::getRoleId, ur.getRoleId());
                return rolePermissionMapper.selectList(rolePermWrapper).stream()
                    .map(RolePermission::getPermissionCode);
            })
            .distinct()
            .collect(Collectors.toList());
        
        // 通过权限代码查询权限ID
        if (!rolePermissionCodes.isEmpty()) {
            LambdaQueryWrapper<Permission> permWrapper = new LambdaQueryWrapper<>();
            permWrapper.in(Permission::getCode, rolePermissionCodes);
            List<Permission> rolePermissions = permissionMapper.selectList(permWrapper);
            permissionIds.addAll(rolePermissions.stream()
                .map(Permission::getId)
                .collect(Collectors.toSet()));
        }
        
        // 查询所有权限
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        
        LambdaQueryWrapper<Permission> finalWrapper = new LambdaQueryWrapper<>();
        finalWrapper.in(Permission::getId, permissionIds);
        finalWrapper.eq(Permission::getStatus, 1);
        finalWrapper.orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectList(finalWrapper);
    }
    
    @Override
    public void assignPermissionToUser(Long userId, Long permissionId) {
        // 检查是否已分配
        LambdaQueryWrapper<UserPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPermission::getUserId, userId);
        wrapper.eq(UserPermission::getPermissionId, permissionId);
        if (userPermissionMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户已拥有该权限");
        }
        
        UserPermission userPermission = new UserPermission();
        userPermission.setUserId(userId);
        userPermission.setPermissionId(permissionId);
        userPermission.setCreateTime(LocalDateTime.now());
        userPermissionMapper.insert(userPermission);
    }
    
    @Override
    public void removePermissionFromUser(Long userId, Long permissionId) {
        LambdaQueryWrapper<UserPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPermission::getUserId, userId);
        wrapper.eq(UserPermission::getPermissionId, permissionId);
        userPermissionMapper.delete(wrapper);
    }
    
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 获取用户的所有权限
        List<Permission> userPermissions = getUserPermissions(userId);
        // 检查是否有指定权限
        return userPermissions.stream()
            .anyMatch(p -> p.getCode().equals(permissionCode));
    }
}

