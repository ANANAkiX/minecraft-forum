-- ============================================
-- 权限数据清理脚本（执行版）
-- 功能：清理role_permission表中的脏数据和无效权限
-- 执行前请先备份数据库！
-- ============================================

-- 步骤1：查看需要清理的数据（先执行这个查看结果）
-- SELECT * FROM role_permission WHERE permission_code IN ('user:read', 'user:update', 'user:delete');

-- 步骤2：删除无效的权限关联
-- user:read, user:update, user:delete 在代码中未使用，应该使用 admin:user:* 系列权限
DELETE FROM role_permission 
WHERE permission_code IN ('user:read', 'user:update', 'user:delete');

-- 步骤3：删除role_permission表中关联的权限代码在permission表中不存在的记录（孤儿数据）
DELETE rp FROM role_permission rp
LEFT JOIN permission p ON rp.permission_code = p.code
WHERE p.code IS NULL;

-- 步骤4：删除role_permission表中关联的角色在role表中不存在的记录（孤儿数据）
DELETE rp FROM role_permission rp
LEFT JOIN role r ON rp.role_id = r.id
WHERE r.id IS NULL;

-- 步骤5：验证清理结果
-- 查看剩余的权限关联，确保权限代码在permission表中存在
SELECT 
    rp.id,
    r.name as role_name,
    rp.permission_code,
    rp.permission_name,
    CASE 
        WHEN p.code IS NULL THEN '❌ 权限不存在'
        ELSE '✅ 权限有效'
    END as status
FROM role_permission rp
LEFT JOIN role r ON rp.role_id = r.id
LEFT JOIN permission p ON rp.permission_code = p.code
ORDER BY rp.role_id, rp.permission_code;

-- 步骤6：统计每个角色的权限数量
SELECT 
    r.id,
    r.name as role_name,
    r.code as role_code,
    COUNT(rp.id) as permission_count
FROM role r
LEFT JOIN role_permission rp ON r.id = rp.role_id
GROUP BY r.id, r.name, r.code
ORDER BY r.id;




