package com.minecraftforum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.dto.PermissionTreeNode;
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
import java.util.*;
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
    
    @Override
    public List<PermissionTreeNode> getPermissionTree(boolean includeDisabled) {
        // 获取所有权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(Permission::getStatus, 1);
        }
        wrapper.orderByAsc(Permission::getSortOrder);
        wrapper.orderByAsc(Permission::getCreateTime);
        List<Permission> allPermissions = permissionMapper.selectList(wrapper);
        
        // 转换为树节点
        Map<Long, PermissionTreeNode> nodeMap = new HashMap<>();
        List<PermissionTreeNode> rootNodes = new ArrayList<>();
        
        // 第一遍：创建所有节点
        for (Permission permission : allPermissions) {
            PermissionTreeNode node = new PermissionTreeNode(permission);
            nodeMap.put(permission.getId(), node);
        }
        
        // 第二遍：构建树结构
        for (Permission permission : allPermissions) {
            PermissionTreeNode node = nodeMap.get(permission.getId());
            Long parentId = permission.getParentId();
            
            if (parentId == null || parentId == 0) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点
                PermissionTreeNode parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                } else {
                    // 父节点不存在，作为根节点处理
                    rootNodes.add(node);
                }
            }
        }
        
        // 对每个节点的子节点进行排序
        sortTreeNodes(rootNodes);
        
        return rootNodes;
    }
    
    /**
     * 递归排序树节点
     */
    private void sortTreeNodes(List<PermissionTreeNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        nodes.sort((a, b) -> {
            int sortCompare = Integer.compare(
                a.getSortOrder() != null ? a.getSortOrder() : 0,
                b.getSortOrder() != null ? b.getSortOrder() : 0
            );
            if (sortCompare != 0) {
                return sortCompare;
            }
            // 如果排序相同，按ID排序
            return Long.compare(a.getId(), b.getId());
        });
        // 递归排序子节点
        for (PermissionTreeNode node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTreeNodes(node.getChildren());
            }
        }
    }
    
    @Override
    public List<Long> expandPermissionsWithParents(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<Long> expandedIds = new HashSet<>(permissionIds);
        
        // 获取所有权限
        List<Permission> allPermissions = permissionMapper.selectList(null);
        Map<Long, Permission> permissionMap = allPermissions.stream()
            .collect(Collectors.toMap(Permission::getId, p -> p));
        
        // 递归查找并添加父权限
        Set<Long> processed = new HashSet<>();
        for (Long permissionId : permissionIds) {
            addParentPermissions(permissionId, permissionMap, expandedIds, processed);
        }
        
        return new ArrayList<>(expandedIds);
    }
    
    /**
     * 递归添加父权限（如果操作权限的父权限是页面访问权限，则自动添加）
     */
    private void addParentPermissions(Long permissionId, Map<Long, Permission> permissionMap, 
                                     Set<Long> expandedIds, Set<Long> processed) {
        if (processed.contains(permissionId)) {
            return; // 避免循环引用
        }
        processed.add(permissionId);
        
        Permission permission = permissionMap.get(permissionId);
        if (permission == null) {
            return;
        }
        
        Long parentId = permission.getParentId();
        if (parentId != null && parentId != 0) {
            Permission parentPermission = permissionMap.get(parentId);
            if (parentPermission != null && parentPermission.getStatus() == 1) {
                // 如果当前权限是操作权限，且父权限是页面访问权限，则自动添加父权限
                if ("ACTION".equals(permission.getType()) && "PAGE".equals(parentPermission.getType())) {
                    expandedIds.add(parentId);
                    // 递归处理父权限的父权限
                    addParentPermissions(parentId, permissionMap, expandedIds, processed);
                }
            }
        }
    }
}

