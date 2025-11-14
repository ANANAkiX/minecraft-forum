package com.minecraftforum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.entity.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> getAllPermissions();
    List<Permission> getEnabledPermissions();
    List<Permission> getPermissionsByType(String type);
    IPage<Permission> getPermissionList(Page<Permission> page, String keyword, String type);
    Permission getPermissionById(Long id);
    Permission getPermissionByCode(String code);
    Permission createPermission(Permission permission);
    Permission updatePermission(Permission permission);
    void deletePermission(Long id);
    List<Permission> getUserPermissions(Long userId);
    // 以下方法已废弃：直接分配权限功能已移除，权限统一通过角色管理
    // void assignPermissionToUser(Long userId, Long permissionId);
    // void removePermissionFromUser(Long userId, Long permissionId);
    // void batchUpdateUserPermissions(Long userId, List<Long> permissionIds);
    boolean hasPermission(Long userId, String permissionCode);
}

