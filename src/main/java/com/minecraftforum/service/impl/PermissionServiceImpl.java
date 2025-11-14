package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.RolePermission;
import com.minecraftforum.entity.UserRole;
import com.minecraftforum.mapper.PermissionMapper;
import com.minecraftforum.mapper.RolePermissionMapper;
import com.minecraftforum.mapper.UserRoleMapper;
import com.minecraftforum.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionMapper permissionMapper;
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
    public IPage<Permission> getPermissionList(Page<Permission> page, String keyword, String type) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（权限名称或权限代码）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Permission::getName, keyword)
                    .or().like(Permission::getCode, keyword));
        }
        
        // 类型筛选
        if (StringUtils.hasText(type)) {
            wrapper.eq(Permission::getType, type);
        }
        
        // 按排序顺序和创建时间排序
        wrapper.orderByAsc(Permission::getSortOrder);
        wrapper.orderByDesc(Permission::getCreateTime);
        
        return permissionMapper.selectPage(page, wrapper);
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
        // 只通过角色获取用户权限（已移除直接分配权限功能）
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        
        if (userRoles.isEmpty()) {
            return List.of();
        }
        
        // 获取用户通过角色获得的权限代码
        List<String> rolePermissionCodes = userRoles.stream()
            .flatMap(ur -> {
                LambdaQueryWrapper<RolePermission> rolePermWrapper = new LambdaQueryWrapper<>();
                rolePermWrapper.eq(RolePermission::getRoleId, ur.getRoleId());
                return rolePermissionMapper.selectList(rolePermWrapper).stream()
                    .map(RolePermission::getPermissionCode);
            })
            .distinct()
            .collect(Collectors.toList());
        
        // 通过权限代码查询权限
        if (rolePermissionCodes.isEmpty()) {
            return List.of();
        }
        
        LambdaQueryWrapper<Permission> permWrapper = new LambdaQueryWrapper<>();
        permWrapper.in(Permission::getCode, rolePermissionCodes);
        permWrapper.eq(Permission::getStatus, 1);
        permWrapper.orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectList(permWrapper);
    }
    
    // 以下方法已废弃：直接分配权限功能已移除，权限统一通过角色管理
    // 如需分配权限，请通过角色管理功能为用户分配角色
    
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 获取用户的所有权限
        List<Permission> userPermissions = getUserPermissions(userId);
        // 检查是否有指定权限
        return userPermissions.stream()
            .anyMatch(p -> p.getCode().equals(permissionCode));
    }
}

