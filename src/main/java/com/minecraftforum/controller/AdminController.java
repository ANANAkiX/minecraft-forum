package com.minecraftforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.common.Result;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.Role;
import com.minecraftforum.entity.RolePermission;
import com.minecraftforum.entity.User;
import com.minecraftforum.entity.UserRole;
import com.minecraftforum.entity.Resource;
import com.minecraftforum.mapper.PermissionMapper;
import com.minecraftforum.mapper.RoleMapper;
import com.minecraftforum.mapper.RolePermissionMapper;
import com.minecraftforum.mapper.UserRoleMapper;
import com.minecraftforum.mapper.ResourceTagMapper;
import com.minecraftforum.service.*;
import com.minecraftforum.dto.ResourceDTO;
import com.minecraftforum.dto.ForumPostDTO;
import com.minecraftforum.dto.AssignRoleRequest;
import com.minecraftforum.dto.RemoveRoleRequest;
import com.minecraftforum.dto.AssignRolePermissionRequest;
import com.minecraftforum.dto.UpdateUserRequest;
import com.minecraftforum.dto.UpdateUserStatusRequest;
import com.minecraftforum.dto.UpdateRoleRequest;
import com.minecraftforum.dto.DeleteRequest;
import com.minecraftforum.dto.UpdateResourceRequest;
import com.minecraftforum.dto.UpdatePostRequest;
import com.minecraftforum.dto.BatchUpdateRolePermissionsRequest;
import com.minecraftforum.dto.UpdatePermissionRequest;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.entity.ResourceTag;
import com.minecraftforum.entity.SysFile;
import com.minecraftforum.dto.PermissionTreeNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minecraftforum.util.SecurityUtil;
import com.minecraftforum.util.ApiScanner;
import com.minecraftforum.util.TokenUtil;
import com.minecraftforum.event.UserPermissionUpdateEvent;
import org.springframework.context.ApplicationEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台管理控制器
 * 处理用户管理、角色管理、权限管理等后台管理功能
 */
@Tag(name = "后台管理", description = "用户管理、角色管理、权限管理等后台管理接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;
    private final FileService fileService;
    private final PermissionService permissionService;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final ResourceService resourceService;
    private final ForumService forumService;
    private final ApiCacheService apiCacheService;
    private final PermissionCacheService permissionCacheService;
    private final ResourceTagMapper resourceTagMapper;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * 获取用户列表
     */
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持关键词搜索，需要admin:user:read")
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(@Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page, @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize, @Parameter(description = "搜索关键词（用户名、昵称、邮箱）") @RequestParam(required = false) String keyword, HttpServletRequest request) {


        Page<User> pageObj = new Page<>(page, pageSize);
        IPage<User> result = userService.getUserList(pageObj, keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());

        return Result.success(data);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "管理员创建新用户，需要admin:user:create或admin:user:manage权限")
    @PostMapping("/users")
    public Result<User> createUser(@Parameter(description = "用户信息", required = true) @RequestBody Map<String, Object> body, HttpServletRequest request) {


        String username = (String) body.get("username");
        String password = (String) body.get("password"); // 可选
        String nickname = (String) body.get("nickname");
        String email = (String) body.get("email");
        Object statusObj = body.get("status");
        Integer status = statusObj != null ? ((Number) statusObj).intValue() : 0;

        // 验证必填字段
        if (username == null || username.isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (email == null || email.isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }

        try {
            User created = userService.createUser(username, password, nickname, email, status);
            // 清除密码信息，确保不会返回给前端
            created.clearPassword();
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新用户的昵称、邮箱、状态等信息，需要admin:user:update或admin:user:manage权限")
    @PutMapping("/users")
    public Result<User> updateUserInfo(@Parameter(description = "用户信息", required = true) @RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {


        if (request.getId() == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userService.getUserById(request.getId());
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 更新字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        User updated = userService.updateUser(user);
        // 清除密码信息，确保不会返回给前端
        updated.clearPassword();
        return Result.success(updated);
    }

    /**
     * 获取用户角色列表
     */
    @Operation(summary = "获取用户角色列表", description = "获取指定用户的所有角色，需要admin:user:read或admin:user:manage权限")
    @GetMapping("/users/roles")
    public Result<List<com.minecraftforum.entity.Role>> getUserRoles(@Parameter(description = "用户ID", required = true) @RequestParam Long id, HttpServletRequest request) {

        if (id == null) {
            return Result.error(400, "用户ID不能为空");
        }

        // 查询用户的所有角色
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);

        List<com.minecraftforum.entity.Role> roles = userRoles.stream().map(ur -> roleMapper.selectById(ur.getRoleId())).filter(Objects::nonNull).collect(Collectors.toList());

        return Result.success(roles);
    }

    /**
     * 为用户分配角色
     */
    @Operation(summary = "为用户分配角色", description = "为用户分配角色，需要admin:user:update或admin:user:manage权限")
    @PostMapping("/users/roles")
    public Result<Void> assignUserRole(@Parameter(description = "分配角色信息", required = true) @RequestBody AssignRoleRequest request, HttpServletRequest httpRequest) {


        if (request.getUserId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (request.getRoleId() == null) {
            return Result.error(400, "角色ID不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 检查是否已经分配
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, request.getUserId());
        wrapper.eq(UserRole::getRoleId, request.getRoleId());
        if (userRoleMapper.selectOne(wrapper) != null) {
            return Result.error(400, "用户已拥有该角色");
        }

        // 分配角色
        UserRole userRole = new UserRole();
        userRole.setUserId(request.getUserId());
        userRole.setRoleId(request.getRoleId());
        userRoleMapper.insert(userRole);

        // 发布权限更新事件，异步处理
        eventPublisher.publishEvent(new UserPermissionUpdateEvent(this, request.getUserId()));

        return Result.success(null);
    }

    /**
     * 移除用户角色
     */
    @Operation(summary = "移除用户角色", description = "移除用户的角色，需要admin:user:update或admin:user:manage权限")
    @DeleteMapping("/users/roles")
    public Result<Void> removeUserRole(@Parameter(description = "移除角色信息", required = true) @RequestBody RemoveRoleRequest request, HttpServletRequest httpRequest) {


        if (request.getUserId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (request.getRoleId() == null) {
            return Result.error(400, "角色ID不能为空");
        }

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, request.getUserId());
        wrapper.eq(UserRole::getRoleId, request.getRoleId());
        userRoleMapper.delete(wrapper);

        // 发布权限更新事件，异步处理
        eventPublisher.publishEvent(new UserPermissionUpdateEvent(this, request.getUserId()));

        return Result.success(null);
    }

    // 以下接口已废弃：直接分配权限功能已移除，权限统一通过角色管理
    // 如需查看用户权限，请通过角色管理功能查看用户拥有的角色及其权限
    // 如需分配权限，请通过角色管理功能为用户分配角色
    // 注意：getAllPermissions 方法已移除，请使用 getPermissionList 方法

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "更新用户的状态（启用/禁用），需要admin:user:update或admin:user:manage权限")
    @PutMapping("/users/status")
    public Result<User> updateUserStatus(@Parameter(description = "状态信息", required = true) @RequestBody UpdateUserStatusRequest request, HttpServletRequest httpRequest) {


        if (request.getUserId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            return Result.error(400, "状态参数错误");
        }

        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        user.setStatus(request.getStatus());
        User updated = userService.updateUser(user);
        updated.setPassword(null);
        return Result.success(updated);
    }

    @GetMapping("/roles")
    @Operation(summary = "获取角色列表", description = "获取角色列表，需要admin:role:read 或者 admin:role:manage权限 ")
    public Result<List<Role>> getAllRoles(HttpServletRequest request) {

        List<Role> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色", description = "创建新角色，需要admin:role:create或admin:role:manage权限")
    @PostMapping("/roles")
    public Result<Role> createRole(@Parameter(description = "角色信息", required = true) @RequestBody Role role, HttpServletRequest request) {


        // 验证必填字段
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            return Result.error(400, "角色名称不能为空");
        }
        if (role.getCode() == null || role.getCode().trim().isEmpty()) {
            return Result.error(400, "角色代码不能为空");
        }

        // 验证角色代码唯一性
        LambdaQueryWrapper<Role> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(Role::getCode, role.getCode());
        if (roleMapper.selectOne(codeWrapper) != null) {
            return Result.error(400, "角色代码已存在");
        }

        // 验证角色名称唯一性
        LambdaQueryWrapper<Role> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Role::getName, role.getName());
        if (roleMapper.selectOne(nameWrapper) != null) {
            return Result.error(400, "角色名称已存在");
        }

        // 设置默认值
        if (role.getStatus() == null) {
            role.setStatus(1); // 默认启用
        }
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());

        roleMapper.insert(role);
        return Result.success(role);
    }

    @PutMapping("/roles")
    @Operation(summary = "修改角色", description = "修改角色显示名称等 代码不可修改，需要admin:role:update:role:manage权限")
    public Result<Role> updateRole(@RequestBody UpdateRoleRequest request, HttpServletRequest httpRequest) {


        if (request.getId() == null) {
            return Result.error(400, "角色ID不能为空");
        }

        // 检查角色是否存在
        Role existingRole = roleMapper.selectById(request.getId());
        if (existingRole == null) {
            return Result.error(404, "角色不存在");
        }

        // 验证必填字段
        if (request.getName() != null && request.getName().trim().isEmpty()) {
            return Result.error(400, "角色名称不能为空");
        }

        // 验证角色名称唯一性（排除自身）
        if (request.getName() != null && !request.getName().equals(existingRole.getName())) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role> nameWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            nameWrapper.eq(Role::getName, request.getName());
            if (roleMapper.selectOne(nameWrapper) != null) {
                return Result.error(400, "角色名称已存在");
            }
        }

        // 更新字段
        if (request.getName() != null) {
            existingRole.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingRole.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            existingRole.setStatus(request.getStatus());
        }
        existingRole.setUpdateTime(LocalDateTime.now());

        roleMapper.updateById(existingRole);
        return Result.success(existingRole);
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "删除角色，需要admin:role:delete或admin:role:manage权限")
    @DeleteMapping("/roles")
    public Result<Void> deleteRole(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) {


        if (request.getId() == null) {
            return Result.error(400, "角色ID不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(request.getId());
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 检查是否有用户使用该角色
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getRoleId, request.getId());
        long userCount = userRoleMapper.selectCount(userRoleWrapper);
        if (userCount > 0) {
            return Result.error(400, "该角色正在被 " + userCount + " 个用户使用，无法删除");
        }

        // 删除角色的所有权限关联
        LambdaQueryWrapper<RolePermission> rolePermWrapper = new LambdaQueryWrapper<>();
        rolePermWrapper.eq(RolePermission::getRoleId, request.getId());
        rolePermissionMapper.delete(rolePermWrapper);

        // 删除角色
        roleMapper.deleteById(request.getId());
        return Result.success(null);
    }

    /**
     * 获取角色权限列表
     */
    @Operation(summary = "获取角色权限列表", description = "获取指定角色的所有权限，需要admin:role:read或admin:role:manage权限")
    @GetMapping("/roles/permissions")
    public Result<List<Permission>> getRolePermissions(@Parameter(description = "角色ID", required = true) @RequestParam Long id, HttpServletRequest request) {

        if (id == null) {
            return Result.error(400, "角色ID不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 查询角色的权限
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(wrapper);

        // 根据permission_code获取完整的权限信息
        List<String> permissionCodes = rolePermissions.stream().map(RolePermission::getPermissionCode).collect(Collectors.toList());

        if (permissionCodes.isEmpty()) {
            return Result.success(List.of());
        }

        LambdaQueryWrapper<Permission> permWrapper = new LambdaQueryWrapper<>();
        permWrapper.in(Permission::getCode, permissionCodes);
        permWrapper.eq(Permission::getStatus, 1);
        List<Permission> permissions = permissionMapper.selectList(permWrapper);

        return Result.success(permissions);
    }

    /**
     * 为角色分配权限
     */
    @Operation(summary = "为角色分配权限", description = "为角色分配权限，需要admin:role:manage或admin:permission:manage权限")
    @PostMapping("/roles/permissions")
    public Result<Void> assignPermissionToRole(@Parameter(description = "分配权限信息", required = true) @RequestBody AssignRolePermissionRequest request, HttpServletRequest httpRequest) {


        if (request.getRoleId() == null) {
            return Result.error(400, "角色ID不能为空");
        }
        if (request.getPermissionId() == null) {
            return Result.error(400, "权限ID不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(request.getPermissionId());
        if (permission == null) {
            return Result.error(404, "权限不存在");
        }

        // 检查是否已经分配
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, request.getRoleId());
        wrapper.eq(RolePermission::getPermissionCode, permission.getCode());
        if (rolePermissionMapper.selectOne(wrapper) != null) {
            return Result.error(400, "角色已拥有该权限");
        }

        // 分配权限
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(request.getRoleId());
        rolePermission.setPermissionCode(permission.getCode());
        rolePermission.setPermissionName(permission.getName());
        rolePermission.setCreateTime(LocalDateTime.now());
        rolePermissionMapper.insert(rolePermission);

        // 发布权限更新事件，异步处理（更新所有拥有该角色的用户）
        eventPublisher.publishEvent(new UserPermissionUpdateEvent(this, request.getRoleId(), true));

        return Result.success(null);
    }

    /**
     * 移除角色权限
     */
    @Operation(summary = "移除角色权限", description = "移除角色的权限，需要admin:role:manage或admin:permission:manage权限")
    @DeleteMapping("/roles/permissions")
    public Result<Void> removePermissionFromRole(@RequestBody AssignRolePermissionRequest request, HttpServletRequest httpRequest) {


        if (request.getRoleId() == null) {
            return Result.error(400, "角色ID不能为空");
        }
        if (request.getPermissionId() == null) {
            return Result.error(400, "权限ID不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 获取权限信息
        Permission permission = permissionMapper.selectById(request.getPermissionId());
        if (permission == null) {
            return Result.error(404, "权限不存在");
        }

        // 删除角色权限关联
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, request.getRoleId());
        wrapper.eq(RolePermission::getPermissionCode, permission.getCode());
        rolePermissionMapper.delete(wrapper);

        // 发布权限更新事件，异步处理（更新所有拥有该角色的用户）
        eventPublisher.publishEvent(new UserPermissionUpdateEvent(this, request.getRoleId(), true));

        return Result.success(null);
    }

    /**
     * 批量更新角色权限
     */
    @Operation(summary = "批量更新角色权限", description = "批量更新角色的权限，需要admin:role:manage或admin:permission:manage权限")
    @PutMapping("/roles/permissions")
    public Result<Void> batchUpdateRolePermissions(@Parameter(description = "批量更新权限信息", required = true) @RequestBody BatchUpdateRolePermissionsRequest request, HttpServletRequest httpRequest) {


        if (request.getRoleId() == null) {
            return Result.error(400, "角色ID不能为空");
        }
        if (request.getPermissionIds() == null) {
            return Result.error(400, "权限ID列表不能为空");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(request.getRoleId());
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        try {
            // 扩展权限列表：自动添加父权限（如果操作权限的父权限是页面访问权限）
            List<Long> expandedPermissionIds = permissionService.expandPermissionsWithParents(request.getPermissionIds());
            
            // 获取当前角色权限
            LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RolePermission::getRoleId, request.getRoleId());
            List<RolePermission> currentRolePermissions = rolePermissionMapper.selectList(wrapper);

            // 获取目标权限代码（使用扩展后的权限ID列表）
            List<Permission> targetPermissions = expandedPermissionIds.isEmpty() ? List.of() : permissionMapper.selectBatchIds(expandedPermissionIds);
            Set<String> targetPermissionCodes = targetPermissions.stream().map(Permission::getCode).collect(java.util.stream.Collectors.toSet());

            // 获取当前权限代码
            Set<String> currentPermissionCodes = currentRolePermissions.stream().map(RolePermission::getPermissionCode).collect(java.util.stream.Collectors.toSet());

            // 需要添加的权限
            Set<String> toAdd = new java.util.HashSet<>(targetPermissionCodes);
            toAdd.removeAll(currentPermissionCodes);

            // 需要删除的权限
            Set<String> toRemove = new java.util.HashSet<>(currentPermissionCodes);
            toRemove.removeAll(targetPermissionCodes);

            // 添加新权限
            for (String permissionCode : toAdd) {
                Permission permission = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>().eq(Permission::getCode, permissionCode));
                if (permission != null) {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(request.getRoleId());
                    rolePermission.setPermissionCode(permission.getCode());
                    rolePermission.setPermissionName(permission.getName());
                    rolePermission.setCreateTime(LocalDateTime.now());
                    rolePermissionMapper.insert(rolePermission);
                }
            }

            // 删除不需要的权限
            if (!toRemove.isEmpty()) {
                LambdaQueryWrapper<RolePermission> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(RolePermission::getRoleId, request.getRoleId());
                deleteWrapper.in(RolePermission::getPermissionCode, toRemove);
                rolePermissionMapper.delete(deleteWrapper);
            }

            // 发布权限更新事件，异步处理（更新所有拥有该角色的用户）
            eventPublisher.publishEvent(new UserPermissionUpdateEvent(this, request.getRoleId(), true));

            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取所有资源列表（管理员专用，包括所有状态）
     */
    @Operation(summary = "获取所有资源列表", description = "管理员获取所有资源列表，包括待审核、已通过、已拒绝的资源，需要admin:resource:manage权限")
    @GetMapping("/resources")
    public Result<Map<String, Object>> getAllResourceList(@Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page, @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize, @Parameter(description = "分类代码") @RequestParam(required = false) String category, @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword, @Parameter(description = "作者ID") @RequestParam(required = false) Long authorId, @Parameter(description = "状态筛选（PENDING/APPROVED/REJECTED）") @RequestParam(required = false) String status, HttpServletRequest request) {


        Page<Resource> pageObj = new Page<>(page, pageSize);
        IPage<ResourceDTO> result = resourceService.getAllResourceList(pageObj, category, keyword, authorId, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());
        return Result.success(data);
    }

    /**
     * 管理员更新资源
     */
    @Operation(summary = "管理员更新资源", description = "管理员更新资源信息，需要admin:resource:manage权限")
    @PutMapping("/resources")
    public Result<ResourceDTO> updateResource(@Parameter(description = "资源信息", required = true) @RequestBody UpdateResourceRequest request, HttpServletRequest httpRequest) {


        if (request.getId() == null) {
            return Result.error(400, "资源ID不能为空");
        }

        ResourceDTO existing = resourceService.getResourceById(request.getId());
        if (existing == null) {
            return Result.error(404, "资源不存在");
        }

        Resource resource = new Resource();
        resource.setId(request.getId());
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setContent(request.getContent());
        resource.setCategory(request.getCategory());
        resource.setVersion(request.getVersion());
        
        resourceService.updateResource(resource);
        
        // 更新标签（如果提供了标签）
        if (request.getTags() != null) {
            // 删除旧标签
            LambdaQueryWrapper<ResourceTag> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ResourceTag::getResourceId, request.getId());
            resourceTagMapper.delete(deleteWrapper);
            
            // 解析并保存新标签
            String tags = request.getTags();
            if (!tags.isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<String> tagList;
                    if (tags.startsWith("[")) {
                        // JSON 格式的标签数组
                        tagList = objectMapper.readValue(tags, List.class);
                    } else {
                        // 逗号分隔的标签字符串
                        tagList = new ArrayList<>();
                        String[] tagArray = tags.split(",");
                        for (String tag : tagArray) {
                            if (!tag.trim().isEmpty()) {
                                tagList.add(tag.trim());
                            }
                        }
                    }
                    
                    // 保存新标签
                    for (String tagName : tagList) {
                        ResourceTag tagEntity = new ResourceTag();
                        tagEntity.setResourceId(request.getId());
                        tagEntity.setTagName(tagName);
                        resourceTagMapper.insert(tagEntity);
                    }
                } catch (Exception e) {
                    // 标签解析失败，忽略继续
                }
            }
        }
        
        ResourceDTO updated = resourceService.getResourceById(request.getId());
        return Result.success(updated);
    }

    /**
     * 管理员更新帖子
     */
    @Operation(summary = "管理员更新帖子", description = "管理员更新帖子信息，需要admin:post:manage权限")
    @PutMapping("/posts")
    public Result<ForumPost> updatePost(@Parameter(description = "帖子信息", required = true) @RequestBody UpdatePostRequest request, HttpServletRequest httpRequest) {


        if (request.getId() == null) {
            return Result.error(400, "帖子ID不能为空");
        }

        ForumPostDTO existing = forumService.getPostById(request.getId());
        if (existing == null) {
            return Result.error(404, "帖子不存在");
        }

        ForumPost post = new ForumPost();
        post.setId(request.getId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        
        ForumPost updated = forumService.updatePost(post);
        return Result.success(updated);
    }

    /**
     * 获取文件列表
     */
    @Operation(summary = "获取文件列表", description = "分页获取文件列表，支持按文件名搜索和资源ID筛选")
    @GetMapping("/files")
    public Result<Map<String, Object>> getFileList(@Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page, @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize, @Parameter(description = "搜索关键词（文件名）") @RequestParam(required = false) String keyword, @Parameter(description = "资源ID（可选）") @RequestParam(required = false) Long resourceId, HttpServletRequest request) {


        Page<SysFile> pageObj = new Page<>(page, pageSize);
        IPage<SysFile> result = fileService.getFileList(pageObj, keyword, resourceId);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());

        return Result.success(data);
    }

    /**
     * 删除文件
     */
    @Operation(summary = "删除文件", description = "删除文件，需要admin:file:delete权限")
    @DeleteMapping("/files")
    public Result<Void> deleteFile(@RequestBody DeleteRequest request) {
        if (request.getId() == null) {
            return Result.error(400, "文件ID不能为空");
        }
        fileService.deleteFile(request.getId());
        return Result.success(null);
    }

    /**
     * 获取权限列表
     */
    @Operation(summary = "获取权限列表", description = "分页获取权限列表，支持按类型筛选和关键词搜索")
    @GetMapping("/permissions")
    public Result<Map<String, Object>> getPermissionList(@Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page, @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize, @Parameter(description = "搜索关键词（权限名称或权限代码）") @RequestParam(required = false) String keyword, @Parameter(description = "权限类型（PAGE/ACTION）") @RequestParam(required = false) String type, HttpServletRequest request) {


        Page<Permission> pageObj = new Page<>(page, pageSize);
        IPage<Permission> result = permissionService.getPermissionList(pageObj, keyword, type);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("pageSize", result.getSize());

        return Result.success(data);
    }

    /**
     * 获取权限树结构
     */
    @Operation(summary = "获取权限树", description = "获取权限的树形结构，支持包含禁用权限")
    @GetMapping("/permissions/tree")
    public Result<List<PermissionTreeNode>> getPermissionTree(@Parameter(description = "是否包含禁用的权限") @RequestParam(defaultValue = "false") Boolean includeDisabled, HttpServletRequest request) {
        List<PermissionTreeNode> tree = permissionService.getPermissionTree(includeDisabled);
        return Result.success(tree);
    }

    /**
     * 创建权限
     */
    @Operation(summary = "创建权限", description = "创建新权限，需要admin:permission:create或admin:permission:manage权限")
    @PostMapping("/permissions")
    public Result<Permission> createPermission(@Parameter(description = "权限信息", required = true) @RequestBody Permission permission, HttpServletRequest request) {


        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        Permission created = permissionService.createPermission(permission);
        
        // 刷新权限缓存
        permissionCacheService.refreshPermissionCache();
        
        return Result.success(created);
    }

    /**
     * 更新权限
     */
    @Operation(summary = "更新权限", description = "更新权限信息，需要admin:permission:update或admin:permission:manage权限")
    @PutMapping("/permissions")
    public Result<Permission> updatePermission(@Parameter(description = "权限信息", required = true) @RequestBody UpdatePermissionRequest request, HttpServletRequest httpRequest) {


        if (request.getId() == null) {
            return Result.error(400, "权限ID不能为空");
        }

        Permission permission = new Permission();
        permission.setId(request.getId());
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType());
        permission.setDescription(request.getDescription());
        permission.setRouter(request.getRouter());
        permission.setApiurl(request.getApiurl());
        permission.setMethodtype(request.getMethodtype());
        permission.setParentId(request.getParentId());
        permission.setSortOrder(request.getSortOrder());
        permission.setStatus(request.getStatus());
        permission.setUpdateTime(LocalDateTime.now());
        
        Permission updated = permissionService.updatePermission(permission);
        
        // 刷新权限缓存
        permissionCacheService.refreshPermissionCache();
        
        return Result.success(updated);
    }

    /**
     * 删除权限
     */
    @Operation(summary = "删除权限", description = "删除权限，需要admin:permission:delete或admin:permission:manage权限")
    @DeleteMapping("/permissions")
    public Result<Void> deletePermission(@RequestBody DeleteRequest request, HttpServletRequest httpRequest) {


        if (request.getId() == null) {
            return Result.error(400, "权限ID不能为空");
        }

        permissionService.deletePermission(request.getId());
        
        // 刷新权限缓存
        permissionCacheService.refreshPermissionCache();
        
        return Result.success(null);
    }

    /**
     * 获取所有API信息
     */
    @Operation(summary = "获取所有API信息", description = "获取系统中所有Controller的API路径、请求方式和描述信息，需要admin:permission:read或admin:permission:manage权限")
    @GetMapping("/apis")
    public Result<List<ApiScanner.ApiInfo>> getAllApis() {
        // 从 Redis 缓存获取 API 列表
        List<ApiScanner.ApiInfo> apiList = apiCacheService.getApiList();
        return Result.success(apiList);
    }
}

