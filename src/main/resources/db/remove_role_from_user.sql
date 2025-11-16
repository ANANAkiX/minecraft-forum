-- 移除 user 表中的 role 字段
-- 角色管理统一通过 user_role 表进行

ALTER TABLE `user` DROP COLUMN `role`;


