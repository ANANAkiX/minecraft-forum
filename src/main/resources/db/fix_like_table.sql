-- 修复 like 表名问题（MySQL 保留关键字）
-- 将表名从 `like` 改为 `user_like`

RENAME TABLE `like` TO `user_like`;

