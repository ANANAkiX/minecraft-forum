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
    
    /**
     * 获取权限树结构
     * @param includeDisabled 是否包含禁用的权限
     * @return 权限树节点列表
     */
    List<com.minecraftforum.dto.PermissionTreeNode> getPermissionTree(boolean includeDisabled);
    
    /**
     * 根据权限ID列表，自动添加父权限（如果操作权限的父权限是页面访问权限，则自动添加父权限）
     * @param permissionIds 权限ID列表
     * @return 包含父权限的完整权限ID列表
     */
    List<Long> expandPermissionsWithParents(List<Long> permissionIds);
}

