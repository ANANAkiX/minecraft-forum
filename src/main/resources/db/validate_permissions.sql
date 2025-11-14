-- ============================================
-- 权限数据验证脚本
-- 功能：验证权限数据的完整性和有效性
-- ============================================

-- 1. 检查role_permission表中是否有重复的role_id和permission_code组合
-- （理论上不应该有，因为有UNIQUE索引）
SELECT 
    role_id, 
    permission_code, 
    COUNT(*) as count,
    GROUP_CONCAT(id) as record_ids
FROM role_permission
GROUP BY role_id, permission_code
HAVING COUNT(*) > 1;

-- 2. 检查role_permission表中的权限代码是否都在permission表中存在
SELECT 
    rp.id,
    rp.role_id,
    rp.permission_code,
    rp.permission_name,
    '权限代码在permission表中不存在' as issue
FROM role_permission rp
LEFT JOIN permission p ON rp.permission_code = p.code
WHERE p.code IS NULL;

-- 3. 检查role_permission表中的角色ID是否都在role表中存在
SELECT 
    rp.id,
    rp.role_id,
    rp.permission_code,
    '角色ID在role表中不存在' as issue
FROM role_permission rp
LEFT JOIN role r ON rp.role_id = r.id
WHERE r.id IS NULL;

-- 4. 检查代码中实际使用的权限是否都在role_permission表中
-- 页面访问权限
SELECT 'page:home' as permission_code, 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'page:home') 
            THEN '✅ 存在' ELSE '❌ 缺失' END as status
UNION ALL
SELECT 'page:forum', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'page:forum') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'page:upload', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'page:upload') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'page:admin', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'page:admin') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
-- 后台管理权限
UNION ALL
SELECT 'admin:user:manage', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:user:manage') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:user:read', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:user:read') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:user:update', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:user:update') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:user:delete', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:user:delete') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:role:manage', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:role:manage') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:role:read', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:role:read') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:role:create', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:role:create') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:role:update', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:role:update') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:role:delete', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:role:delete') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:resource:manage', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:resource:manage') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:post:manage', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:post:manage') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:category:manage', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:category:manage') 
            THEN '✅ 存在' ELSE '❌ 缺失' END
UNION ALL
SELECT 'admin:permission:manage', 
       CASE WHEN EXISTS (SELECT 1 FROM role_permission WHERE permission_code = 'admin:permission:manage') 
            THEN '✅ 存在' ELSE '❌ 缺失' END;

-- 5. 列出所有无效的权限代码（在role_permission中存在但在代码中未使用）
-- 这些是应该被清理的权限
SELECT DISTINCT
    rp.permission_code,
    rp.permission_name,
    COUNT(*) as usage_count
FROM role_permission rp
WHERE rp.permission_code IN (
    'user:read', 
    'user:update', 
    'user:delete'
)
GROUP BY rp.permission_code, rp.permission_name;

-- 6. 查看每个角色拥有的所有权限
SELECT 
    r.id as role_id,
    r.name as role_name,
    r.code as role_code,
    rp.permission_code,
    rp.permission_name,
    p.type as permission_type,
    p.status as permission_status
FROM role r
INNER JOIN role_permission rp ON r.id = rp.role_id
LEFT JOIN permission p ON rp.permission_code = p.code
ORDER BY r.id, rp.permission_code;




