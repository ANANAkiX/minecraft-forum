package com.minecraftforum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraftforum.common.Result;
import com.minecraftforum.entity.Permission;
import com.minecraftforum.entity.Role;
import com.minecraftforum.entity.RolePermission;
import com.minecraftforum.entity.User;
import com.minecraftforum.entity.UserPermission;
import com.minecraftforum.entity.UserRole;
import com.minecraftforum.mapper.PermissionMapper;
import com.minecraftforum.mapper.RoleMapper;
import com.minecraftforum.mapper.RolePermissionMapper;
import com.minecraftforum.mapper.UserPermissionMapper;
import com.minecraftforum.mapper.UserRoleMapper;
import com.minecraftforum.service.PermissionService;
import com.minecraftforum.service.UserService;
import com.minecraftforum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;
    private final UserPermissionMapper userPermissionMapper;
    
    /**
     * 检查用户是否有指定权限
     */
    private boolean checkPermission(HttpServletRequest request, String permissionCode) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            return false;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }
        // 兼容旧的角色判断：如果是ADMIN角色，默认有所有权限
        User currentUser = userService.getUserById(userId);
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            return true;
        }
        // 检查权限
        return permissionService.hasPermission(userId, permissionCode);
    }
    
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
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
    
    @PutMapping("/users/{id}/role")
    public Result<User> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:user:update或admin:user:manage权限
        if (!checkPermission(request, "admin:user:update") && !checkPermission(request, "admin:user:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        String role = body.get("role");
        if (role == null || (!role.equals("USER") && !role.equals("ADMIN"))) {
            return Result.error(400, "角色参数错误");
        }
        
        User updated = userService.updateUserRole(id, role);
        return Result.success(updated);
    }
    
    @GetMapping("/users/{id}/roles")
    public Result<List<com.minecraftforum.entity.Role>> getUserRoles(
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
            .filter(r -> r != null)
            .collect(Collectors.toList());
        
        return Result.success(roles);
    }
    
    @PostMapping("/users/{id}/roles")
    public Result<Void> assignUserRole(
            @PathVariable Long id,
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
    
    @DeleteMapping("/users/{id}/roles/{roleId}")
    public Result<Void> removeUserRole(
            @PathVariable Long id,
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
    
    @GetMapping("/users/{id}/permissions")
    public Result<Map<String, Object>> getUserPermissions(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        // 获取用户的所有权限（包括通过角色获得的权限和直接分配的权限）
        List<Permission> allPermissions = permissionService.getUserPermissions(id);
        
        // 获取用户直接分配的权限
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserPermission> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(UserPermission::getUserId, id);
        List<UserPermission> userPermissions = userPermissionMapper.selectList(wrapper);
        List<Long> directPermissionIds = userPermissions.stream()
            .map(UserPermission::getPermissionId)
            .collect(Collectors.toList());
        
        List<Permission> directPermissions = directPermissionIds.isEmpty() ? List.of() :
            permissionMapper.selectBatchIds(directPermissionIds);
        
        // 获取用户的所有角色
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole> userRoleWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, id);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);
        
        // 构建权限来源映射：权限代码 -> 角色名称列表
        Map<String, List<String>> permissionSourceMap = new HashMap<>();
        
        // 标记直接分配的权限
        for (Permission perm : directPermissions) {
            permissionSourceMap.put(perm.getCode(), List.of("直接分配"));
        }
        
        // 标记通过角色获得的权限
        for (UserRole userRole : userRoles) {
            Role role = roleMapper.selectById(userRole.getRoleId());
            if (role == null) continue;
            
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RolePermission> rolePermWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            rolePermWrapper.eq(RolePermission::getRoleId, userRole.getRoleId());
            List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermWrapper);
            
            for (RolePermission rolePerm : rolePermissions) {
                String permCode = rolePerm.getPermissionCode();
                permissionSourceMap.computeIfAbsent(permCode, k -> new java.util.ArrayList<>()).add(role.getName());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("all", allPermissions);
        result.put("direct", directPermissions);
        result.put("sources", permissionSourceMap); // 权限来源映射
        
        return Result.success(result);
    }
    
    @PostMapping("/users/{id}/permissions")
    public Result<Void> assignPermissionToUser(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        Long permissionId = body.get("permissionId");
        if (permissionId == null) {
            return Result.error(400, "权限ID不能为空");
        }
        
        try {
            permissionService.assignPermissionToUser(id, permissionId);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
    
    @DeleteMapping("/users/{id}/permissions/{permissionId}")
    public Result<Void> removePermissionFromUser(
            @PathVariable Long id,
            @PathVariable Long permissionId,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        permissionService.removePermissionFromUser(id, permissionId);
        return Result.success(null);
    }
    
    @GetMapping("/permissions")
    public Result<List<Permission>> getAllPermissions(
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        
        // 验证权限：需要admin:permission:manage权限
        if (!checkPermission(request, "admin:permission:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        List<Permission> permissions;
        if (type != null && !type.isEmpty()) {
            permissions = permissionService.getPermissionsByType(type);
        } else {
            permissions = permissionService.getEnabledPermissions();
        }
        return Result.success(permissions);
    }
    
    @PutMapping("/users/{id}/status")
    public Result<User> updateUserStatus(
            @PathVariable Long id,
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
    public Result<List<Role>> getAllRoles(HttpServletRequest request) {
        // 验证权限：需要admin:role:read或admin:role:manage权限
        if (!checkPermission(request, "admin:role:read") && !checkPermission(request, "admin:role:manage")) {
            return Result.error(403, "无权限访问");
        }
        
        List<Role> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }
    
    @PostMapping("/roles")
    public Result<Role> createRole(
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
    
    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(
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
    
    @GetMapping("/roles/{id}/permissions")
    public Result<List<Permission>> getRolePermissions(
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
    
    @PostMapping("/roles/{id}/permissions")
    public Result<Void> assignPermissionToRole(
            @PathVariable Long id,
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
    
    @DeleteMapping("/roles/{id}/permissions/{permissionId}")
    public Result<Void> removePermissionFromRole(
            @PathVariable Long id,
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
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

