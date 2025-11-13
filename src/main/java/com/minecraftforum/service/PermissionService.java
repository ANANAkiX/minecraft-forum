package com.minecraftforum.service;

import com.minecraftforum.entity.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> getAllPermissions();
    List<Permission> getEnabledPermissions();
    List<Permission> getPermissionsByType(String type);
    Permission getPermissionById(Long id);
    Permission getPermissionByCode(String code);
    Permission createPermission(Permission permission);
    Permission updatePermission(Permission permission);
    void deletePermission(Long id);
    List<Permission> getUserPermissions(Long userId);
    void assignPermissionToUser(Long userId, Long permissionId);
    void removePermissionFromUser(Long userId, Long permissionId);
    boolean hasPermission(Long userId, String permissionCode);
}

