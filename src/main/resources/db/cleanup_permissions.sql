-- ============================================
-- 权限数据清理脚本
-- 功能：清理role_permission表中的脏数据和无效权限
-- ============================================

-- 1. 查看当前role_permission表中的重复权限（同一role_id和permission_code）
-- 注意：由于有UNIQUE索引，理论上不应该有重复，但检查一下是否有异常数据
SELECT 
    role_id, 
    permission_code, 
    COUNT(*) as count
FROM role_permission
GROUP BY role_id, permission_code
HAVING COUNT(*) > 1;

-- 2. 查看代码中实际使用的权限列表（这些权限应该保留）
-- 页面访问权限
-- page:home, page:forum, page:upload, page:admin
-- page:home:all, page:home:pack, page:home:mod, page:home:resource

-- 资源相关权限
-- resource:create, resource:read, resource:update, resource:delete

-- 帖子相关权限
-- post:create, post:read, post:update, post:delete

-- 评论相关权限
-- comment:create, comment:update, comment:delete

-- 后台管理权限（这些是实际使用的）
-- admin:user:manage, admin:user:read, admin:user:update, admin:user:delete
-- admin:role:manage, admin:role:read, admin:role:create, admin:role:update, admin:role:delete
-- admin:resource:manage
-- admin:post:manage
-- admin:category:manage
-- admin:permission:manage

-- 3. 删除无效的权限关联（user:read, user:update, user:delete 在代码中未使用）
-- 这些权限应该被 admin:user:* 系列权限替代
DELETE FROM role_permission 
WHERE permission_code IN ('user:read', 'user:update', 'user:delete');

-- 4. 删除role_permission表中关联的权限代码在permission表中不存在的记录（孤儿数据）
DELETE rp FROM role_permission rp
LEFT JOIN permission p ON rp.permission_code = p.code
WHERE p.code IS NULL;

-- 5. 删除role_permission表中关联的角色在role表中不存在的记录（孤儿数据）
DELETE rp FROM role_permission rp
LEFT JOIN role r ON rp.role_id = r.id
WHERE r.id IS NULL;

-- 6. 验证清理后的数据
-- 查看剩余的权限关联
SELECT 
    rp.id,
    r.name as role_name,
    rp.permission_code,
    rp.permission_name,
    p.code as permission_code_in_table,
    p.name as permission_name_in_table
FROM role_permission rp
LEFT JOIN role r ON rp.role_id = r.id
LEFT JOIN permission p ON rp.permission_code = p.code
ORDER BY rp.role_id, rp.permission_code;

-- 7. 统计每个角色的权限数量
SELECT 
    r.name as role_name,
    COUNT(rp.id) as permission_count
FROM role r
LEFT JOIN role_permission rp ON r.id = rp.role_id
GROUP BY r.id, r.name
ORDER BY r.id;

-- 8. 检查是否有权限代码不一致的情况（role_permission中的permission_code在permission表中不存在）
SELECT 
    rp.permission_code,
    rp.permission_name,
    COUNT(*) as count
FROM role_permission rp
LEFT JOIN permission p ON rp.permission_code = p.code
WHERE p.code IS NULL
GROUP BY rp.permission_code, rp.permission_name;

-- ============================================
-- 可选：如果需要完全重建role_permission表，可以使用以下脚本
-- ============================================

-- 备份当前数据（可选）
-- CREATE TABLE role_permission_backup AS SELECT * FROM role_permission;

-- 清空role_permission表（谨慎操作）
-- TRUNCATE TABLE role_permission;

-- 重新插入有效的权限关联（根据实际需求调整）
-- 示例：为管理员角色(role_id=2)分配所有权限
-- INSERT INTO role_permission (role_id, permission_code, permission_name, create_time)
-- SELECT 2, code, name, NOW()
-- FROM permission
-- WHERE status = 1;

-- 示例：为普通用户角色(role_id=1)分配基础权限
-- INSERT INTO role_permission (role_id, permission_code, permission_name, create_time)
-- SELECT 1, code, name, NOW()
-- FROM permission
-- WHERE code IN (
--     'page:home', 'page:forum', 'page:upload',
--     'page:home:all', 'page:home:pack', 'page:home:mod', 'page:home:resource',
--     'resource:create', 'resource:read', 'resource:update',
--     'post:create', 'post:read', 'post:update',
--     'comment:create', 'comment:update'
-- )
-- AND status = 1;




