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
import com.minecraftforum.service.PermissionService;
import com.minecraftforum.service.UserService;
import com.minecraftforum.service.ResourceService;
import com.minecraftforum.service.ForumService;
import com.minecraftforum.dto.ResourceDTO;
import com.minecraftforum.dto.ForumPostDTO;
import com.minecraftforum.entity.ForumPost;
import com.minecraftforum.util.SecurityUtil;
import com.minecraftforum.util.ApiScanner;
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
    private final com.minecraftforum.service.FileService fileService;
    private final PermissionService permissionService;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final ResourceService resourceService;
    private final ForumService forumService;
    private final SecurityUtil securityUtil;
    private final ApplicationContext applicationContext;
    private final ApiScanner apiScanner;

    /**
     * 检查当前用户是否有指定权限（从JWT中获取权限，不查询数据库）
     */
    private boolean checkPermission(String permissionCode) {
        // 从Spring Security的SecurityContext中获取权限（这些权限来自JWT Token）
        return securityUtil.hasPermission(permissionCode);
    }

    /**
     * 检查当前用户是否有指定权限（重载方法，兼容现有代码）
     */
    private boolean checkPermission(HttpServletRequest request, String permissionCode) {
        // 从Spring Security的SecurityContext中获取权限（这些权限来自JWT Token）
        return securityUtil.hasPermission(permissionCode);
    }

    /**
     * 获取用户列表
     */
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持关键词搜索，需要admin:user:read")
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词（用户名、昵称、邮箱）")
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        // 验证权限：需要admin:user:read或admin:user:manage权限
        if (!checkPermission(request, "admin:user:read") && !checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }

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
     * 更新用户角色
     */
    @Operation(summary = "更新用户角色", description = "更新用户的角色（USER/ADMIN），需要admin:user:manage权限")
    @PutMapping("/users/{id}/role")
    public Result<User> updateUserRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "角色信息", required = true)
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        // 验证权限：需要admin:user:manage权限
        if (!checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }

        String role = body.get("role");
        if (role == null || (!role.equals("USER") && !role.equals("ADMIN"))) {
            return Result.error(400, "角色参数错误");
        }

        User updated = userService.updateUserRole(id, role);
        return Result.success(updated);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新用户的昵称、邮箱、角色、状态等信息，需要admin:user:update或admin:user:manage权限")
    @PutMapping("/users/{id}")
    public Result<User> updateUserInfo(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "用户信息", required = true)
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        // 验证权限：需要admin:user:update或admin:user:manage权限
        if (!checkPermission(request, "admin:user:update") && !checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }

        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 更新昵称
        if (body.containsKey("nickname")) {
            user.setNickname((String) body.get("nickname"));
        }

        // 更新邮箱
        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }

        // 更新角色
        if (body.containsKey("role")) {
            String role = (String) body.get("role");
            if (role != null && (role.equals("USER") || role.equals("ADMIN"))) {
                user.setRole(role);
            }
        }

        // 更新状态
        if (body.containsKey("status")) {
            Object statusObj = body.get("status");
            if (statusObj instanceof Number) {
                user.setStatus(((Number) statusObj).intValue());
            }
        }

        User updated = userService.updateUser(user);
        updated.setPassword(null);
        return Result.success(updated);
    }

    /**
     * 获取用户角色列表
     */
    @Operation(summary = "获取用户角色列表", description = "获取指定用户的所有角色，需要admin:user:read或admin:user:manage权限")
    @GetMapping("/users/{id}/roles")
    public Result<List<com.minecraftforum.entity.Role>> getUserRoles(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {

        // 验证权限：需要admin:user:read或admin:user:manage权限
        if (!checkPermission(request, "admin:user:read") && !checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 查询用户的所有角色
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);

        List<com.minecraftforum.entity.Role> roles = userRoles.stream()
            .map(ur -> roleMapper.selectById(ur.getRoleId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return Result.success(roles);
    }

    /**
     * 为用户分配角色
     */
    @Operation(summary = "为用户分配角色", description = "为用户分配角色，需要admin:user:update或admin:user:manage权限")
    @PostMapping("/users/{id}/roles")
    public Result<Void> assignUserRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "角色信息", required = true)
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) {

        // 验证权限：需要admin:user:update或admin:user:manage权限
        if (!checkPermission(request, "admin:user:update") && !checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }

        Long roleId = body.get("roleId");
        if (roleId == null) {
            return Result.error(400, "角色ID不能为空");
        }

        // 检查角色是否存在
        com.minecraftforum.entity.Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 检查是否已经分配
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        wrapper.eq(UserRole::getRoleId, roleId);
        if (userRoleMapper.selectOne(wrapper) != null) {
            return Result.error(400, "用户已拥有该角色");
        }

        // 分配角色
        UserRole userRole = new UserRole();
        userRole.setUserId(id);
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);

        return Result.success(null);
    }

    /**
     * 移除用户角色
     */
    @Operation(summary = "移除用户角色", description = "移除用户的角色，需要admin:user:update或admin:user:manage权限")
    @DeleteMapping("/users/{id}/roles/{roleId}")
    public Result<Void> removeUserRole(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId,
            HttpServletRequest request) {

        // 验证权限：需要admin:user:update或admin:user:manage权限
        if (!checkPermission(request, "admin:user:update") && !checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        wrapper.eq(UserRole::getRoleId, roleId);
        userRoleMapper.delete(wrapper);

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
    @PutMapping("/users/{id}/status")
    public Result<User> updateUserStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "状态信息", required = true)
            @RequestBody Map<String, Integer> body,
            HttpServletRequest request) {

        // 验证权限：需要admin:user:update或admin:user:manage权限
        if (!checkPermission(request, "admin:user:update") && !checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }

        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "状态参数错误");
        }

        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        user.setStatus(status);
        User updated = userService.updateUser(user);
        updated.setPassword(null);
        return Result.success(updated);
    }

    @GetMapping("/roles")
    @Operation(summary = "获取角色列表", description = "获取角色列表，需要admin:role:read 或者 admin:role:manage权限 ")
    public Result<List<Role>> getAllRoles(HttpServletRequest request) {
        // 验证权限：需要admin:role:read或admin:role:manage权限
        if (!checkPermission(request, "admin:role:read") && !checkPermission(request, "admin:role:manage")) {
            return Result.error(403, "无权限访问");
        }

        List<Role> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色", description = "创建新角色，需要admin:role:create或admin:role:manage权限")
    @PostMapping("/roles")
    public Result<Role> createRole(
            @Parameter(description = "角色信息", required = true)
            @RequestBody Role role,
            HttpServletRequest request) {

        // 验证权限：需要admin:role:create或admin:role:manage权限
        if (!checkPermission(request, "admin:role:create") && !checkPermission(request, "admin:role:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 验证必填字段
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            return Result.error(400, "角色名称不能为空");
        }
        if (role.getCode() == null || role.getCode().trim().isEmpty()) {
            return Result.error(400, "角色代码不能为空");
        }

        // 验证角色代码唯一性
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role> codeWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        codeWrapper.eq(Role::getCode, role.getCode());
        if (roleMapper.selectOne(codeWrapper) != null) {
            return Result.error(400, "角色代码已存在");
        }

        // 验证角色名称唯一性
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role> nameWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
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

    @PutMapping("/roles/{id}")
    @Operation(summary = "修改角色", description = "修改角色显示名称等 代码不可修改，需要admin:role:update:role:manage权限")
    public Result<Role> updateRole(
            @PathVariable Long id,
            @RequestBody Role role,
            HttpServletRequest request) {

        // 验证权限：需要admin:role:update或admin:role:manage权限
        if (!checkPermission(request, "admin:role:update") && !checkPermission(request, "admin:role:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 检查角色是否存在
        Role existingRole = roleMapper.selectById(id);
        if (existingRole == null) {
            return Result.error(404, "角色不存在");
        }

        // 验证必填字段
        if (role.getName() != null && role.getName().trim().isEmpty()) {
            return Result.error(400, "角色名称不能为空");
        }
        if (role.getCode() != null && role.getCode().trim().isEmpty()) {
            return Result.error(400, "角色代码不能为空");
        }

        // 验证角色代码唯一性（排除自身）
        if (role.getCode() != null && !role.getCode().equals(existingRole.getCode())) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role> codeWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            codeWrapper.eq(Role::getCode, role.getCode());
            if (roleMapper.selectOne(codeWrapper) != null) {
                return Result.error(400, "角色代码已存在");
            }
        }

        // 验证角色名称唯一性（排除自身）
        if (role.getName() != null && !role.getName().equals(existingRole.getName())) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role> nameWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            nameWrapper.eq(Role::getName, role.getName());
            if (roleMapper.selectOne(nameWrapper) != null) {
                return Result.error(400, "角色名称已存在");
            }
        }

        // 更新字段
        if (role.getName() != null) {
            existingRole.setName(role.getName());
        }
        if (role.getCode() != null) {
            existingRole.setCode(role.getCode());
        }
        if (role.getDescription() != null) {
            existingRole.setDescription(role.getDescription());
        }
        if (role.getStatus() != null) {
            existingRole.setStatus(role.getStatus());
        }
        existingRole.setUpdateTime(LocalDateTime.now());

        roleMapper.updateById(existingRole);
        return Result.success(existingRole);
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "删除角色，需要admin:role:delete或admin:role:manage权限")
    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {

        // 验证权限：需要admin:role:delete或admin:role:manage权限
        if (!checkPermission(request, "admin:role:delete") && !checkPermission(request, "admin:role:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 检查是否有用户使用该角色
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole> userRoleWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getRoleId, id);
        long userCount = userRoleMapper.selectCount(userRoleWrapper);
        if (userCount > 0) {
            return Result.error(400, "该角色正在被 " + userCount + " 个用户使用，无法删除");
        }

        // 删除角色的所有权限关联
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> rolePermWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        rolePermWrapper.eq(RolePermission::getRoleId, id);
        rolePermissionMapper.delete(rolePermWrapper);

        // 删除角色
        roleMapper.deleteById(id);
        return Result.success(null);
    }

    /**
     * 获取角色权限列表
     */
    @Operation(summary = "获取角色权限列表", description = "获取指定角色的所有权限，需要admin:role:read或admin:role:manage权限")
    @GetMapping("/roles/{id}/permissions")
    public Result<List<Permission>> getRolePermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {

        // 验证权限：需要admin:role:read或admin:role:manage权限
        if (!checkPermission(request, "admin:role:read") && !checkPermission(request, "admin:role:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 查询角色的权限
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(wrapper);

        // 根据permission_code获取完整的权限信息
        List<String> permissionCodes = rolePermissions.stream()
            .map(RolePermission::getPermissionCode)
            .collect(Collectors.toList());

        if (permissionCodes.isEmpty()) {
            return Result.success(List.of());
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Permission> permWrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        permWrapper.in(Permission::getCode, permissionCodes);
        permWrapper.eq(Permission::getStatus, 1);
        List<Permission> permissions = permissionMapper.selectList(permWrapper);

        return Result.success(permissions);
    }

    /**
     * 为角色分配权限
     */
    @Operation(summary = "为角色分配权限", description = "为角色分配权限，需要admin:role:manage或admin:permission:manage权限")
    @PostMapping("/roles/{id}/permissions")
    public Result<Void> assignPermissionToRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "权限信息", required = true)
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) {

        // 验证权限：需要admin:role:manage或admin:permission:manage权限
        if (!checkPermission(request, "admin:role:manage") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        Long permissionId = body.get("permissionId");
        if (permissionId == null) {
            return Result.error(400, "权限ID不能为空");
        }

        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            return Result.error(404, "权限不存在");
        }

        // 检查是否已经分配
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        wrapper.eq(RolePermission::getPermissionCode, permission.getCode());
        if (rolePermissionMapper.selectOne(wrapper) != null) {
            return Result.error(400, "角色已拥有该权限");
        }

        // 分配权限
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(id);
        rolePermission.setPermissionCode(permission.getCode());
        rolePermission.setPermissionName(permission.getName());
        rolePermission.setCreateTime(LocalDateTime.now());
        rolePermissionMapper.insert(rolePermission);

        return Result.success(null);
    }

    /**
     * 移除角色权限
     */
    @Operation(summary = "移除角色权限", description = "移除角色的权限，需要admin:role:manage或admin:permission:manage权限")
    @DeleteMapping("/roles/{id}/permissions/{permissionId}")
    public Result<Void> removePermissionFromRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long permissionId,
            HttpServletRequest request) {

        // 验证权限：需要admin:role:manage或admin:permission:manage权限
        if (!checkPermission(request, "admin:role:manage") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 获取权限信息
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            return Result.error(404, "权限不存在");
        }

        // 删除角色权限关联
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(RolePermission::getRoleId, id);
        wrapper.eq(RolePermission::getPermissionCode, permission.getCode());
        rolePermissionMapper.delete(wrapper);

        return Result.success(null);
    }

    /**
     * 批量更新角色权限
     */
    @Operation(summary = "批量更新角色权限", description = "批量更新角色的权限，需要admin:role:manage或admin:permission:manage权限")
    @PutMapping("/roles/{id}/permissions")
    public Result<Void> batchUpdateRolePermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "权限ID列表", required = true)
            @RequestBody Map<String, List<Long>> body,
            HttpServletRequest request) {

        // 验证权限：需要admin:role:manage或admin:permission:manage权限
        if (!checkPermission(request, "admin:role:manage") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }

        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        List<Long> permissionIds = body.get("permissionIds");
        if (permissionIds == null) {
            return Result.error(400, "权限ID列表不能为空");
        }

        try {
            // 获取当前角色权限
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(RolePermission::getRoleId, id);
            List<RolePermission> currentRolePermissions = rolePermissionMapper.selectList(wrapper);

            // 获取目标权限代码
            List<Permission> targetPermissions = permissionIds.isEmpty() ? List.of() :
                permissionMapper.selectBatchIds(permissionIds);
            Set<String> targetPermissionCodes = targetPermissions.stream()
                .map(Permission::getCode)
                .collect(java.util.stream.Collectors.toSet());

            // 获取当前权限代码
            Set<String> currentPermissionCodes = currentRolePermissions.stream()
                .map(RolePermission::getPermissionCode)
                .collect(java.util.stream.Collectors.toSet());

            // 需要添加的权限
            Set<String> toAdd = new java.util.HashSet<>(targetPermissionCodes);
            toAdd.removeAll(currentPermissionCodes);

            // 需要删除的权限
            Set<String> toRemove = new java.util.HashSet<>(currentPermissionCodes);
            toRemove.removeAll(targetPermissionCodes);

            // 添加新权限
            for (String permissionCode : toAdd) {
                Permission permission = permissionMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Permission>()
                        .eq(Permission::getCode, permissionCode)
                );
                if (permission != null) {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(id);
                    rolePermission.setPermissionCode(permission.getCode());
                    rolePermission.setPermissionName(permission.getName());
                    rolePermission.setCreateTime(LocalDateTime.now());
                    rolePermissionMapper.insert(rolePermission);
                }
            }

            // 删除不需要的权限
            if (!toRemove.isEmpty()) {
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> deleteWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                deleteWrapper.eq(RolePermission::getRoleId, id);
                deleteWrapper.in(RolePermission::getPermissionCode, toRemove);
                rolePermissionMapper.delete(deleteWrapper);
            }

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
    public Result<Map<String, Object>> getAllResourceList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "分类代码")
            @RequestParam(required = false) String category,
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "作者ID")
            @RequestParam(required = false) Long authorId,
            @Parameter(description = "状态筛选（PENDING/APPROVED/REJECTED）")
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        // 验证权限：需要admin:resource:manage权限
        if (!checkPermission(request, "admin:resource:manage")) {
            return Result.error(403, "无权限访问");
        }

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
    @PutMapping("/resources/{id}")
    public Result<ResourceDTO> updateResource(
            @Parameter(description = "资源ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "资源信息", required = true)
            @RequestBody Resource resource,
            HttpServletRequest request) {

        // 验证权限：需要admin:resource:manage权限
        if (!checkPermission(request, "admin:resource:manage")) {
            return Result.error(403, "无权限访问");
        }

        ResourceDTO existing = resourceService.getResourceById(id);
        if (existing == null) {
            return Result.error(404, "资源不存在");
        }

        resource.setId(id);
        resourceService.updateResource(resource);
        ResourceDTO updated = resourceService.getResourceById(id);
        return Result.success(updated);
    }

    /**
     * 管理员更新帖子
     */
    @Operation(summary = "管理员更新帖子", description = "管理员更新帖子信息，需要admin:post:manage权限")
    @PutMapping("/posts/{id}")
    public Result<ForumPost> updatePost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "帖子信息", required = true)
            @RequestBody ForumPost post,
            HttpServletRequest request) {

        // 验证权限：需要admin:post:manage权限
        if (!checkPermission(request, "admin:post:manage")) {
            return Result.error(403, "无权限访问");
        }

        ForumPostDTO existing = forumService.getPostById(id);
        if (existing == null) {
            return Result.error(404, "帖子不存在");
        }

        post.setId(id);
        ForumPost updated = forumService.updatePost(post);
        return Result.success(updated);
    }
    
    /**
     * 获取文件列表
     */
    @Operation(summary = "获取文件列表", description = "分页获取文件列表，支持按文件名搜索和资源ID筛选")
    @GetMapping("/files")
    public Result<Map<String, Object>> getFileList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词（文件名）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "资源ID（可选）")
            @RequestParam(required = false) Long resourceId,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:file:read或admin:file:manage权限
        if (!checkPermission(request, "admin:file:read") && !checkPermission(request, "admin:file:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        Page<com.minecraftforum.entity.SysFile> pageObj = new Page<>(page, pageSize);
        IPage<com.minecraftforum.entity.SysFile> result = fileService.getFileList(pageObj, keyword, resourceId);
        
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
    @Operation(summary = "删除文件", description = "删除文件，需要admin:file:delete或admin:file:manage权限")
    @DeleteMapping("/files/{id}")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:file:delete或admin:file:manage权限
        if (!checkPermission(request, "admin:file:delete") && !checkPermission(request, "admin:file:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        fileService.deleteFile(id);
        return Result.success(null);
    }
    
    /**
     * 获取权限列表
     */
    @Operation(summary = "获取权限列表", description = "分页获取权限列表，支持按类型筛选和关键词搜索")
    @GetMapping("/permissions")
    public Result<Map<String, Object>> getPermissionList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词（权限名称或权限代码）")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "权限类型（PAGE/ACTION）")
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:read或admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:read") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
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
     * 创建权限
     */
    @Operation(summary = "创建权限", description = "创建新权限，需要admin:permission:create或admin:permission:manage权限")
    @PostMapping("/permissions")
    public Result<Permission> createPermission(
            @Parameter(description = "权限信息", required = true)
            @RequestBody Permission permission,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:create或admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:create") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        Permission created = permissionService.createPermission(permission);
        return Result.success(created);
    }
    
    /**
     * 更新权限
     */
    @Operation(summary = "更新权限", description = "更新权限信息，需要admin:permission:update或admin:permission:manage权限")
    @PutMapping("/permissions/{id}")
    public Result<Permission> updatePermission(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "权限信息", required = true)
            @RequestBody Permission permission,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:update或admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:update") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        permission.setId(id);
        permission.setUpdateTime(LocalDateTime.now());
        Permission updated = permissionService.updatePermission(permission);
        return Result.success(updated);
    }
    
    /**
     * 删除权限
     */
    @Operation(summary = "删除权限", description = "删除权限，需要admin:permission:delete或admin:permission:manage权限")
    @DeleteMapping("/permissions/{id}")
    public Result<Void> deletePermission(
            @Parameter(description = "权限ID", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:delete或admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:delete") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        permissionService.deletePermission(id);
        return Result.success(null);
    }
    
    /**
     * 获取所有API信息
     */
    @Operation(summary = "获取所有API信息", description = "获取系统中所有Controller的API路径、请求方式和描述信息，需要admin:permission:read或admin:permission:manage权限")
    @GetMapping("/apis")
    public Result<List<ApiScanner.ApiInfo>> getAllApis(HttpServletRequest request) {
        // 验证权限：需要admin:permission:read或admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:read") && !checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        List<ApiScanner.ApiInfo> apiList = apiScanner.scanAllApis(applicationContext);
        return Result.success(apiList);
    }
}

