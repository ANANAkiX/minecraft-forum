package com.minecraftforum.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户权限更新事件
 * 当用户权限发生变化时发布此事件，由监听器异步处理权限同步
 */
@Getter
public class UserPermissionUpdateEvent extends ApplicationEvent {
    
    /**
     * 用户ID
     */
    private final Long userId;
    
    /**
     * 是否更新所有拥有该角色的用户（用于角色权限更新）
     */
    private final boolean updateRoleUsers;
    
    /**
     * 角色ID（当updateRoleUsers为true时使用）
     */
    private final Long roleId;
    
    /**
     * 创建用户权限更新事件
     * @param source 事件源
     * @param userId 用户ID
     */
    public UserPermissionUpdateEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
        this.updateRoleUsers = false;
        this.roleId = null;
    }
    
    /**
     * 创建角色权限更新事件（会更新所有拥有该角色的用户）
     * @param source 事件源
     * @param roleId 角色ID
     */
    public UserPermissionUpdateEvent(Object source, Long roleId, boolean updateRoleUsers) {
        super(source);
        this.userId = null;
        this.updateRoleUsers = updateRoleUsers;
        this.roleId = roleId;
    }
}



