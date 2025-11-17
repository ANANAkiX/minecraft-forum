package com.minecraftforum.listener;

import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.UserRole;
import com.minecraftforum.event.UserPermissionUpdateEvent;
import com.minecraftforum.mapper.UserRoleMapper;
import com.minecraftforum.service.PermissionService;
import com.minecraftforum.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限更新事件监听器
 * 异步处理用户权限更新，同步更新Redis中的Token权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionUpdateListener {
    
    private final PermissionService permissionService;
    private final TokenUtil tokenUtil;
    private final UserRoleMapper userRoleMapper;
    
    /**
     * 监听用户权限更新事件
     * 异步处理，不阻塞主线程
     */
    @Async
    @EventListener
    public void handleUserPermissionUpdate(UserPermissionUpdateEvent event) {
        try {
            if (event.isUpdateRoleUsers()) {
                // 更新所有拥有该角色的用户的Token权限
                handleRolePermissionUpdate(event.getRoleId());
            } else {
                // 更新指定用户的Token权限
                handleUserPermissionUpdate(event.getUserId());
            }
        } catch (Exception e) {
            log.error("处理权限更新事件失败: userId={}, roleId={}, error={}", 
                    event.getUserId(), event.getRoleId(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理单个用户的权限更新
     */
    private void handleUserPermissionUpdate(Long userId) {
        try {
            log.debug("开始同步用户Token权限: userId={}", userId);
            
            // 获取用户的最新权限
            List<Permission> permissions = permissionService.getUserPermissions(userId);
            List<String> permissionCodes = permissions.stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toList());
            
            // 更新用户所有Token的权限
            tokenUtil.updateUserAllTokensPermissions(userId, permissionCodes);
            
            log.info("用户Token权限同步完成: userId={}, permissionCount={}", userId, permissionCodes.size());
        } catch (Exception e) {
            log.error("同步用户Token权限失败: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 处理角色权限更新（更新所有拥有该角色的用户）
     */
    private void handleRolePermissionUpdate(Long roleId) {
        try {
            log.debug("开始同步角色用户Token权限: roleId={}", roleId);
            
            // 查询所有拥有该角色的用户
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(UserRole::getRoleId, roleId);
            List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
            
            if (userRoles == null || userRoles.isEmpty()) {
                log.debug("角色没有关联用户: roleId={}", roleId);
                return;
            }
            
            // 为每个用户同步更新Token权限
            int successCount = 0;
            int failCount = 0;
            for (UserRole userRole : userRoles) {
                try {
                    handleUserPermissionUpdate(userRole.getUserId());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.warn("同步用户Token权限失败: userId={}, roleId={}, error={}", 
                            userRole.getUserId(), roleId, e.getMessage());
                }
            }
            
            log.info("角色用户Token权限同步完成: roleId={}, totalUsers={}, success={}, fail={}", 
                    roleId, userRoles.size(), successCount, failCount);
        } catch (Exception e) {
            log.error("同步角色用户Token权限失败: roleId={}, error={}", roleId, e.getMessage(), e);
        }
    }
}



