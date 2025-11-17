/*
 Navicat Premium Dump SQL

 Source Server         : 本地my_sql
 Source Server Type    : MySQL
 Source Server Version : 80018 (8.0.18)
 Source Host           : localhost:3306
 Source Schema         : minecraft_forum

 Target Server Type    : MySQL
 Target Server Version : 80018 (8.0.18)
 File Encoding         : 65001

 Date: 17/11/2025 23:44:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category_config
-- ----------------------------
DROP TABLE IF EXISTS `category_config`;
CREATE TABLE `category_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（显示名称）',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类代码（用于查询）',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RESOURCE' COMMENT '类型：RESOURCE-资源分类',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `is_default` tinyint(4) NULL DEFAULT 0 COMMENT '是否默认显示：0-否，1-是（全部）',
  `status` int(11) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分类配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category_config
-- ----------------------------
INSERT INTO `category_config` VALUES (1, '全部', '', 'RESOURCE', 0, 1, 1, '2025-11-13 05:26:02', '2025-11-13 22:18:17');
INSERT INTO `category_config` VALUES (2, '整合包', 'PACK', 'RESOURCE', 1, 0, 1, '2025-11-13 05:26:02', '2025-11-13 05:26:02');
INSERT INTO `category_config` VALUES (3, 'MOD', 'MOD', 'RESOURCE', 2, 0, 1, '2025-11-13 05:26:02', '2025-11-13 05:26:02');
INSERT INTO `category_config` VALUES (4, '资源包', 'RESOURCE', 'RESOURCE', 3, 0, 1, '2025-11-13 05:26:02', '2025-11-13 05:26:02');
INSERT INTO `category_config` VALUES (10, '分享', 'SHARE', 'FORUM', 1, 0, 1, '2025-11-13 05:29:55', '2025-11-17 23:04:40');
INSERT INTO `category_config` VALUES (11, '求助', 'HELP', 'FORUM', 2, 0, 1, '2025-11-13 05:29:55', '2025-11-13 05:29:55');
INSERT INTO `category_config` VALUES (12, '教程', 'TUTORIAL', 'FORUM', 3, 0, 1, '2025-11-13 05:29:55', '2025-11-15 01:55:53');
INSERT INTO `category_config` VALUES (13, '公告', 'ANNOUNCEMENT', 'FORUM', 4, 0, 1, '2025-11-13 05:29:55', '2025-11-13 05:29:55');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` bigint(20) NULL DEFAULT NULL COMMENT '资源ID（可用于帖子评论）',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 1, 2, '彩色彩色', 0, '2025-11-03 02:29:24');
INSERT INTO `comment` VALUES (2, 2, 2, '测试', 0, '2025-11-03 02:29:42');
INSERT INTO `comment` VALUES (3, 2, 2, '啊', 0, '2025-11-03 02:31:04');
INSERT INTO `comment` VALUES (4, 3, 5, '评论一下', 0, '2025-11-14 03:49:10');
INSERT INTO `comment` VALUES (5, 3, 2, '来个评论', 2, '2025-11-14 19:37:15');
INSERT INTO `comment` VALUES (6, 4, 2, '留下来了', 0, '2025-11-14 20:21:34');
INSERT INTO `comment` VALUES (7, 4, 2, '测试', 0, '2025-11-14 20:25:53');
INSERT INTO `comment` VALUES (8, 4, 2, '测试', 0, '2025-11-14 20:25:54');
INSERT INTO `comment` VALUES (9, 4, 2, '测试', 0, '2025-11-14 20:25:56');
INSERT INTO `comment` VALUES (10, 4, 2, '测试', 0, '2025-11-14 20:25:57');
INSERT INTO `comment` VALUES (11, 4, 2, '测试', 0, '2025-11-14 20:25:59');
INSERT INTO `comment` VALUES (12, 4, 2, '测试', 0, '2025-11-14 20:26:02');
INSERT INTO `comment` VALUES (13, 4, 2, '测试', 0, '2025-11-14 20:26:03');
INSERT INTO `comment` VALUES (14, 4, 2, '测试', 0, '2025-11-14 20:26:04');
INSERT INTO `comment` VALUES (15, 4, 2, '测试', 0, '2025-11-14 20:26:05');
INSERT INTO `comment` VALUES (16, 4, 2, '测试', 1, '2025-11-14 20:26:06');
INSERT INTO `comment` VALUES (17, 4, 2, '测试', 0, '2025-11-14 20:26:07');
INSERT INTO `comment` VALUES (18, 4, 2, '测试', 1, '2025-11-14 20:26:26');
INSERT INTO `comment` VALUES (19, 4, 2, '再次测试发布评论', 1, '2025-11-15 02:44:25');
INSERT INTO `comment` VALUES (20, 3, 2, '发表评论', 0, '2025-11-17 20:37:48');

-- ----------------------------
-- Table structure for download_log
-- ----------------------------
DROP TABLE IF EXISTS `download_log`;
CREATE TABLE `download_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '下载日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of download_log
-- ----------------------------
INSERT INTO `download_log` VALUES (1, 2, 2, '2025-11-03 02:18:27');
INSERT INTO `download_log` VALUES (2, 2, 2, '2025-11-03 02:18:29');
INSERT INTO `download_log` VALUES (3, 2, 2, '2025-11-03 02:18:29');
INSERT INTO `download_log` VALUES (4, 2, 2, '2025-11-03 02:18:29');
INSERT INTO `download_log` VALUES (5, 2, 2, '2025-11-03 02:18:29');
INSERT INTO `download_log` VALUES (6, 2, 2, '2025-11-03 02:18:29');
INSERT INTO `download_log` VALUES (7, 2, 6, '2025-11-13 05:16:10');
INSERT INTO `download_log` VALUES (8, 5, 9, '2025-11-14 07:03:38');
INSERT INTO `download_log` VALUES (9, 5, 10, '2025-11-14 07:09:06');
INSERT INTO `download_log` VALUES (10, 5, 10, '2025-11-14 07:12:27');
INSERT INTO `download_log` VALUES (11, 5, 10, '2025-11-14 07:13:59');
INSERT INTO `download_log` VALUES (12, 5, 10, '2025-11-14 07:17:27');
INSERT INTO `download_log` VALUES (13, 5, 10, '2025-11-14 07:22:07');
INSERT INTO `download_log` VALUES (14, 5, 9, '2025-11-14 07:22:39');
INSERT INTO `download_log` VALUES (15, 5, 10, '2025-11-14 07:43:12');
INSERT INTO `download_log` VALUES (16, 5, 10, '2025-11-14 07:43:29');
INSERT INTO `download_log` VALUES (17, 5, 11, '2025-11-14 08:42:25');
INSERT INTO `download_log` VALUES (18, 5, 11, '2025-11-14 08:45:31');
INSERT INTO `download_log` VALUES (19, 5, 11, '2025-11-14 08:46:00');
INSERT INTO `download_log` VALUES (20, 5, 11, '2025-11-14 09:10:53');
INSERT INTO `download_log` VALUES (21, 5, 11, '2025-11-14 09:11:22');
INSERT INTO `download_log` VALUES (22, 5, 11, '2025-11-14 09:11:27');
INSERT INTO `download_log` VALUES (23, 5, 11, '2025-11-14 09:18:49');
INSERT INTO `download_log` VALUES (24, 2, 18, '2025-11-14 12:23:58');
INSERT INTO `download_log` VALUES (25, 5, 18, '2025-11-14 12:39:28');
INSERT INTO `download_log` VALUES (26, 2, 18, '2025-11-15 02:41:39');
INSERT INTO `download_log` VALUES (27, 2, 18, '2025-11-15 02:42:43');
INSERT INTO `download_log` VALUES (28, 2, 18, '2025-11-15 02:42:45');
INSERT INTO `download_log` VALUES (29, 2, 18, '2025-11-16 12:50:41');
INSERT INTO `download_log` VALUES (30, 2, 12, '2025-11-17 04:35:43');
INSERT INTO `download_log` VALUES (31, 2, 12, '2025-11-17 04:36:09');
INSERT INTO `download_log` VALUES (32, 2, 12, '2025-11-17 04:36:15');
INSERT INTO `download_log` VALUES (33, 2, 18, '2025-11-17 19:09:49');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_resource`(`user_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (3, 2, 2, '2025-11-03 02:19:14');
INSERT INTO `favorite` VALUES (4, 2, 6, '2025-11-13 05:15:55');
INSERT INTO `favorite` VALUES (5, 5, 7, '2025-11-14 03:55:37');
INSERT INTO `favorite` VALUES (6, 5, 10, '2025-11-14 07:43:18');
INSERT INTO `favorite` VALUES (8, 5, 11, '2025-11-14 09:10:46');
INSERT INTO `favorite` VALUES (9, 5, 12, '2025-11-14 12:05:56');
INSERT INTO `favorite` VALUES (10, 2, 12, '2025-11-14 12:07:05');
INSERT INTO `favorite` VALUES (11, 5, 18, '2025-11-14 21:00:34');

-- ----------------------------
-- Table structure for forum_post
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类：SHARE, HELP, TUTORIAL, ANNOUNCEMENT',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int(11) NULL DEFAULT 0 COMMENT '评论数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL, LOCKED, DELETED',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post
-- ----------------------------
INSERT INTO `forum_post` VALUES (1, '测试测试', '测试乘势而上', 'TUTORIAL', 2, 8, 0, 1, 'NORMAL', '2025-11-03 02:28:56', '2025-11-17 23:14:58');
INSERT INTO `forum_post` VALUES (2, '彩色', '彩色是', 'HELP', 2, 9, 0, 2, 'NORMAL', '2025-11-03 02:29:39', '2025-11-17 23:14:58');
INSERT INTO `forum_post` VALUES (3, '测试帖子', '测试求助', 'SHARE', 5, 178, 2, 3, 'NORMAL', '2025-11-14 03:48:10', '2025-11-14 09:11:14');
INSERT INTO `forum_post` VALUES (4, '测试新帖子', '阿萨大大大萨达萨达萨达是', 'TUTORIAL', 2, 74, 2, 14, 'NORMAL', '2025-11-14 20:21:24', '2025-11-16 15:25:40');

-- ----------------------------
-- Table structure for forum_reply
-- ----------------------------
DROP TABLE IF EXISTS `forum_reply`;
CREATE TABLE `forum_reply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `parent_id` bigint(20) NULL DEFAULT NULL COMMENT '父回复ID，用于支持嵌套回复（如果为null，则是直接回复评论）',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
  `target_user_id` bigint(20) NULL DEFAULT NULL COMMENT '目标用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回复表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_reply
-- ----------------------------
INSERT INTO `forum_reply` VALUES (2, 4, NULL, 2, 5, '再次测试', 0, '2025-11-14 20:19:47');
INSERT INTO `forum_reply` VALUES (3, 5, NULL, 2, 2, '测试测试', 1, '2025-11-14 20:20:16');
INSERT INTO `forum_reply` VALUES (4, 6, NULL, 2, 2, '再次留下一条', 0, '2025-11-14 20:21:41');
INSERT INTO `forum_reply` VALUES (5, 4, NULL, 5, 2, '再次测试回复评论', 0, '2025-11-14 20:31:35');
INSERT INTO `forum_reply` VALUES (6, 16, NULL, 5, 2, '测试', 0, '2025-11-14 21:28:45');
INSERT INTO `forum_reply` VALUES (7, 16, NULL, 5, 5, '测试', 0, '2025-11-14 21:28:50');
INSERT INTO `forum_reply` VALUES (8, 16, NULL, 5, 5, '测试', 0, '2025-11-14 21:37:32');
INSERT INTO `forum_reply` VALUES (9, 18, NULL, 5, 2, '测试', 0, '2025-11-14 21:59:21');
INSERT INTO `forum_reply` VALUES (10, 16, NULL, 5, 5, '测试', 0, '2025-11-14 22:02:19');
INSERT INTO `forum_reply` VALUES (11, 6, NULL, 5, 2, '在测试一条', 0, '2025-11-14 22:14:58');
INSERT INTO `forum_reply` VALUES (12, 18, NULL, 2, 2, '测试回复', 0, '2025-11-15 01:57:59');
INSERT INTO `forum_reply` VALUES (13, 18, NULL, 2, 5, '再次测试回复', 0, '2025-11-15 01:58:06');
INSERT INTO `forum_reply` VALUES (14, 4, NULL, 2, 5, '那我也再次回复', 1, '2025-11-17 20:37:56');

-- ----------------------------
-- Table structure for like
-- ----------------------------
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `resource_id` bigint(20) NULL DEFAULT NULL COMMENT '资源ID',
  `post_id` bigint(20) NULL DEFAULT NULL COMMENT '帖子ID',
  `comment_id` bigint(20) NULL DEFAULT NULL COMMENT '评论ID',
  `reply_id` bigint(20) NULL DEFAULT NULL COMMENT '回复ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_resource`(`user_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_user_post`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `idx_user_comment`(`user_id` ASC, `comment_id` ASC) USING BTREE,
  INDEX `idx_user_reply`(`user_id` ASC, `reply_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of like
-- ----------------------------
INSERT INTO `like` VALUES (1, 5, 11, NULL, NULL, NULL, '2025-11-14 09:10:41');
INSERT INTO `like` VALUES (3, 5, 12, NULL, NULL, NULL, '2025-11-14 12:05:55');
INSERT INTO `like` VALUES (5, 2, NULL, 3, NULL, NULL, '2025-11-14 12:07:03');
INSERT INTO `like` VALUES (8, 2, NULL, NULL, 5, NULL, '2025-11-14 20:20:12');
INSERT INTO `like` VALUES (14, 5, NULL, 4, NULL, NULL, '2025-11-14 21:00:26');
INSERT INTO `like` VALUES (15, 5, 18, NULL, NULL, NULL, '2025-11-14 21:00:35');
INSERT INTO `like` VALUES (16, 5, NULL, NULL, 5, NULL, '2025-11-14 21:26:53');
INSERT INTO `like` VALUES (17, 5, NULL, NULL, NULL, 3, '2025-11-14 21:26:54');
INSERT INTO `like` VALUES (18, 5, NULL, 3, NULL, NULL, '2025-11-14 21:26:56');
INSERT INTO `like` VALUES (19, 5, NULL, NULL, 16, NULL, '2025-11-14 21:28:42');
INSERT INTO `like` VALUES (20, 5, NULL, NULL, 18, NULL, '2025-11-14 21:59:22');
INSERT INTO `like` VALUES (21, 2, NULL, NULL, 19, NULL, '2025-11-15 02:44:30');
INSERT INTO `like` VALUES (22, 2, NULL, 4, NULL, NULL, '2025-11-16 15:55:47');
INSERT INTO `like` VALUES (23, 2, 12, NULL, NULL, NULL, '2025-11-17 04:36:08');
INSERT INTO `like` VALUES (24, 2, NULL, NULL, NULL, 14, '2025-11-17 20:37:59');

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限代码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限类型：PAGE-页面访问，ACTION-操作权限',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限描述',
  `router` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '访问权限的路由地址',
  `apiurl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作权限的API地址',
  `methodtype` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方式(GET/POST/PUT/DELETE)',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父权限ID，0表示顶级权限',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `status` int(11) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_apiurl`(`apiurl` ASC) USING BTREE,
  INDEX `idx_router`(`router` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, 'page:home', '访问首页', 'PAGE', '访问首页', NULL, NULL, NULL, 0, 1, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (2, 'page:forum', '访问论坛', 'PAGE', '访问论坛页面', NULL, NULL, NULL, 0, 2, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (3, 'page:upload', '访问上传资源', 'PAGE', '访问上传资源页面', NULL, NULL, NULL, 0, 3, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (4, 'page:admin', '访问后台管理', 'PAGE', '访问后台管理页面', NULL, NULL, NULL, 0, 4, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (5, 'page:home:all', '访问首页-全部', 'PAGE', '访问首页全部分类', NULL, NULL, NULL, 1, 5, 1, '2025-11-13 06:42:17', '2025-11-17 18:35:20');
INSERT INTO `permission` VALUES (6, 'page:home:pack', '访问首页-整合包', 'PAGE', '访问首页整合包分类', NULL, NULL, NULL, 1, 6, 1, '2025-11-13 06:42:17', '2025-11-17 18:35:20');
INSERT INTO `permission` VALUES (7, 'page:home:mod', '访问首页-MOD', 'PAGE', '访问首页MOD分类', NULL, NULL, NULL, 1, 7, 1, '2025-11-13 06:42:17', '2025-11-17 18:35:20');
INSERT INTO `permission` VALUES (8, 'page:home:resource', '访问首页-资源包', 'PAGE', '访问首页资源包分类', NULL, NULL, NULL, 1, 8, 1, '2025-11-13 06:42:17', '2025-11-17 18:35:20');
INSERT INTO `permission` VALUES (9, 'resource:create', '创建资源', 'ACTION', '创建新的资源，需要resource:create权限', NULL, '/api/resource', 'POST', 3, 10, 1, '2025-11-13 06:42:17', '2025-11-17 18:41:03');
INSERT INTO `permission` VALUES (10, 'resource:read', '查看资源', 'ACTION', '根据ID获取资源的详细信息', NULL, '/api/resource/detail', 'GET', 61, 11, 1, '2025-11-13 06:42:17', '2025-11-17 22:12:57');
INSERT INTO `permission` VALUES (11, 'resource:update', '修改资源', 'ACTION', '更新资源信息，只能更新自己创建的资源', NULL, '/api/resource', 'PUT', 61, 12, 1, '2025-11-13 06:42:17', '2025-11-17 22:13:09');
INSERT INTO `permission` VALUES (12, 'resource:delete', '删除资源', 'ACTION', '删除资源，只能删除自己创建的资源', NULL, '/api/resource', 'DELETE', 61, 13, 1, '2025-11-13 06:42:17', '2025-11-17 22:13:51');
INSERT INTO `permission` VALUES (13, 'post:create', '创建帖子', 'ACTION', '发布新帖子，需要post:create权限', NULL, '/api/forum/posts', 'POST', 61, 20, 1, '2025-11-13 06:42:17', '2025-11-17 18:55:18');
INSERT INTO `permission` VALUES (14, 'post:read', '查看帖子', 'ACTION', '根据ID获取帖子的详细信息', NULL, '/api/forum/posts/detail', 'GET', 61, 21, 1, '2025-11-13 06:42:17', '2025-11-17 22:13:23');
INSERT INTO `permission` VALUES (15, 'post:update', '修改帖子', 'ACTION', '更新帖子信息，只能更新自己发布的帖子', NULL, '/api/forum/posts', 'PUT', 61, 22, 1, '2025-11-13 06:42:17', '2025-11-17 22:13:38');
INSERT INTO `permission` VALUES (16, 'post:delete', '删除帖子', 'ACTION', '删除帖子，只能删除自己发布的帖子', NULL, '/api/forum/posts', 'DELETE', 61, 23, 1, '2025-11-13 06:42:17', '2025-11-17 22:14:00');
INSERT INTO `permission` VALUES (17, 'comment:create', '创建评论', 'ACTION', '为帖子创建评论，发布一条评论 需要comment:create权限', NULL, '/api/forum/posts/{postId}/comments', 'POST', 61, 30, 1, '2025-11-13 06:42:17', '2025-11-17 18:55:00');
INSERT INTO `permission` VALUES (18, 'comment:update', '修改评论', 'ACTION', '修改评论', NULL, NULL, NULL, 61, 31, 1, '2025-11-13 06:42:17', '2025-11-17 18:54:58');
INSERT INTO `permission` VALUES (19, 'comment:delete', '删除评论', 'ACTION', '删除评论，只有评论作者可以删除（级联删除所有子回复）', NULL, '/api/forum/comments', 'DELETE', 61, 32, 1, '2025-11-13 06:42:17', '2025-11-17 22:14:27');
INSERT INTO `permission` VALUES (23, 'admin:user:manage', '后台管理-用户管理', 'PAGE', '管理用户（包含所有用户管理操作）', '', '', '', 4, 50, 1, '2025-11-13 06:42:17', '2025-11-17 19:07:18');
INSERT INTO `permission` VALUES (24, 'admin:user:read', '查看用户列表', 'ACTION', '分页获取用户列表，支持关键词搜索，需要admin:user:read', NULL, '/api/admin/users', 'GET', 23, 51, 1, '2025-11-13 06:42:17', '2025-11-17 18:39:42');
INSERT INTO `permission` VALUES (25, 'admin:user:update', '修改用户', 'ACTION', '更新用户的昵称、邮箱、状态等信息，需要admin:user:update权限', NULL, '/api/admin/users', 'PUT', 23, 52, 1, '2025-11-13 06:42:17', '2025-11-17 22:15:53');
INSERT INTO `permission` VALUES (26, 'admin:user:delete', '删除用户', 'ACTION', '删除用户', NULL, NULL, NULL, 23, 53, 1, '2025-11-13 06:42:17', '2025-11-17 18:39:42');
INSERT INTO `permission` VALUES (27, 'admin:resource:manage', '后台管理-资源管理', 'PAGE', '管理资源', '', '', '', 4, 54, 1, '2025-11-13 06:42:17', '2025-11-17 19:07:18');
INSERT INTO `permission` VALUES (28, 'admin:post:manage', '后台管理-帖子管理', 'PAGE', '管理帖子', '', '', '', 4, 55, 1, '2025-11-13 06:42:17', '2025-11-17 19:07:18');
INSERT INTO `permission` VALUES (29, 'admin:category:manage', '后台管理-分类管理', 'PAGE', '管理分类配置', '', '', '', 4, 56, 1, '2025-11-13 06:42:17', '2025-11-17 19:07:18');
INSERT INTO `permission` VALUES (30, 'admin:permission:manage', '后台管理-权限管理', 'PAGE', '管理权限', '', '', '', 4, 57, 1, '2025-11-13 06:42:17', '2025-11-17 19:07:18');
INSERT INTO `permission` VALUES (31, 'admin:role:manage', '后台管理-角色管理', 'PAGE', '访问管理角色页面', NULL, '', '', 4, 58, 1, '2025-11-13 06:42:17', '2025-11-17 19:07:18');
INSERT INTO `permission` VALUES (32, 'admin:role:read', '查看角色', 'ACTION', '获取角色列表，需要admin:role:read 或者 admin:role:manage权限 ', '', '/api/admin/roles', 'GET', 31, 59, 1, '2025-11-13 06:42:17', '2025-11-17 18:43:35');
INSERT INTO `permission` VALUES (33, 'admin:role:create', '创建角色', 'ACTION', '创建新角色，需要admin:role:create或admin:role:manage权限', NULL, '/api/admin/roles', 'POST', 31, 60, 1, '2025-11-13 06:42:17', '2025-11-17 18:43:35');
INSERT INTO `permission` VALUES (34, 'admin:role:update', '修改角色', 'ACTION', '修改角色显示名称等 代码不可修改，需要admin:role:update:role:manage权限', '', '/api/admin/roles', 'PUT', 31, 61, 1, '2025-11-13 06:42:17', '2025-11-17 22:20:15');
INSERT INTO `permission` VALUES (35, 'admin:role:delete', '删除角色', 'ACTION', '删除角色，需要admin:role:delete或admin:role:manage权限', NULL, '/api/admin/roles', 'DELETE', 31, 62, 1, '2025-11-13 06:42:17', '2025-11-17 22:20:32');
INSERT INTO `permission` VALUES (36, 'resource:download', '下载文件', 'ACTION', '从OSS下载文件，需要resource:download权限', NULL, '/api/files/{id}/download', 'GET', 61, 63, 1, '2025-11-14 07:11:50', '2025-11-17 18:54:34');
INSERT INTO `permission` VALUES (37, 'admin:resource:audit', '审核资源', 'ACTION', '管理员更新资源信息，需要admin:resource:audit权限', '', '/api/admin/resources', 'PUT', 27, 0, 1, '2025-11-15 00:44:26', '2025-11-17 22:16:35');
INSERT INTO `permission` VALUES (38, 'admin:permission:read', '获取所有权限列表', 'ACTION', '分页获取权限列表，支持按类型筛选和关键词搜索', NULL, '/api/admin/permissions', 'GET', 30, 0, 1, '2025-11-15 01:44:31', '2025-11-17 18:40:01');
INSERT INTO `permission` VALUES (41, 'admin:permission:create', '添加权限', 'ACTION', '创建新权限，需要admin:permission:create或admin:permission:manage权限', NULL, '/api/admin/permissions', 'POST', 30, 0, 1, '2025-11-15 01:46:40', '2025-11-17 18:40:01');
INSERT INTO `permission` VALUES (42, 'admin:permission:delete', '删除一个权限', 'ACTION', '删除权限，需要admin:permission:delete或admin:permission:manage权限', '', '/api/admin/permissions', 'DELETE', 30, 0, 1, '2025-11-15 01:47:43', '2025-11-17 22:19:38');
INSERT INTO `permission` VALUES (43, 'admin:permission:update', '编辑权限', 'ACTION', '更新权限信息，需要admin:permission:update或admin:permission:manage权限', '', '/api/admin/permissions', 'PUT', 30, 0, 1, '2025-11-15 01:48:38', '2025-11-17 22:19:56');
INSERT INTO `permission` VALUES (44, 'admin:category:read', '获取分类配置的数据', 'ACTION', '获取所有分类配置，包括已禁用的，需要admin:category:manage权限', '', '/api/category-config', 'GET', 29, 0, 1, '2025-11-15 01:53:11', '2025-11-17 18:43:47');
INSERT INTO `permission` VALUES (45, 'admin:category:create', '添加分类', 'ACTION', '创建新的分类配置，需要admin:category:manage权限', '', '/api/category-config', 'POST', 29, 0, 1, '2025-11-15 01:54:51', '2025-11-17 18:43:47');
INSERT INTO `permission` VALUES (46, 'admin:category:update', '编辑分类', 'ACTION', '更新分类配置信息，需要admin:category:manage权限', '', '/api/category-config', 'PUT', 29, 0, 1, '2025-11-15 01:55:11', '2025-11-17 22:19:19');
INSERT INTO `permission` VALUES (47, 'admin:category:delete', '删除分类', 'ACTION', '删除分类配置，需要admin:category:manage权限', '', '/api/category-config', 'DELETE', 29, 0, 1, '2025-11-15 01:55:25', '2025-11-17 22:19:28');
INSERT INTO `permission` VALUES (48, 'admin:post:read', '获取帖子数据', 'ACTION', '分页获取帖子列表，支持按分类、关键词、作者筛选和排序', '', '/api/forum/posts', 'GET', 28, 0, 1, '2025-11-15 01:57:35', '2025-11-17 18:43:21');
INSERT INTO `permission` VALUES (49, 'admin:post:update', '修改帖子', 'ACTION', '管理员更新帖子信息，需要admin:post:manage权限', '', '/api/admin/posts', 'PUT', 28, 0, 1, '2025-11-15 01:58:47', '2025-11-17 22:18:59');
INSERT INTO `permission` VALUES (50, 'admin:post:delete', '删除一篇帖子', 'ACTION', '删除帖子，只能删除自己发布的帖子', '', '/api/forum/posts', 'DELETE', 28, 0, 1, '2025-11-15 02:00:01', '2025-11-17 22:19:07');
INSERT INTO `permission` VALUES (51, 'admin:post:create', '发布帖子', 'ACTION', '发布新帖子，需要post:create权限', '', '/api/forum/posts', 'POST', 28, 0, 1, '2025-11-15 02:01:56', '2025-11-17 18:43:21');
INSERT INTO `permission` VALUES (52, 'admin:resource:read', '获取资源管理的数据', 'ACTION', '管理员获取所有资源列表，包括待审核、已通过、已拒绝的资源，需要admin:resource:manage权限', '', '/api/admin/resources', 'GET', 27, 0, 1, '2025-11-15 02:02:46', '2025-11-17 18:41:19');
INSERT INTO `permission` VALUES (53, 'admin:resource:update', '修改资源', 'ACTION', '管理员更新资源信息，需要admin:resource:update权限', '', '/api/admin/resources', 'PUT', 27, 0, 1, '2025-11-15 02:03:29', '2025-11-17 22:16:55');
INSERT INTO `permission` VALUES (54, 'admin:resource:delete', '管理员删除资源', 'ACTION', '删除资源，只能删除自己创建的资源', '', '/api/resource', 'DELETE', 27, 0, 1, '2025-11-15 02:04:15', '2025-11-17 22:18:40');
INSERT INTO `permission` VALUES (55, 'admin:user:create', '创建用户', 'ACTION', '管理员创建新用户，需要admin:user:create或admin:user:manage权限', '', '/api/admin/users', 'POST', 23, 0, 1, '2025-11-15 02:05:56', '2025-11-17 18:39:42');
INSERT INTO `permission` VALUES (56, 'user:info', '获取当前用户的信息', 'ACTION', '获取当前登录用户的详细信息，包括权限列表', '', '/api/user/info', 'GET', 61, 0, 1, '2025-11-16 11:31:45', '2025-11-17 22:12:20');
INSERT INTO `permission` VALUES (57, 'elasticsearch:search', '分词搜索', 'ACTION', '搜索帖子和资源，需要 elasticsearch:search 权限', '', '/api/search', 'GET', 61, 0, 1, '2025-11-16 14:10:25', '2025-11-17 22:12:31');
INSERT INTO `permission` VALUES (58, 'admin:file:manage', '后台管理-文件管理', 'PAGE', '访问后台管理中的文件管理', '', '', '', 4, 0, 1, '2025-11-17 04:58:02', '2025-11-17 19:07:18');
INSERT INTO `permission` VALUES (59, 'admin:file:read', '获取文件列表', 'ACTION', '分页获取文件列表，支持按文件名搜索和资源ID筛选', '', '/api/admin/files', 'GET', 58, 0, 1, '2025-11-17 05:02:37', '2025-11-17 18:38:44');
INSERT INTO `permission` VALUES (60, 'admin:file:delete', '删除文件', 'ACTION', '删除文件，需要admin:file:delete权限', '', '/api/admin/files', 'DELETE', 58, 0, 1, '2025-11-17 05:03:04', '2025-11-17 22:15:21');
INSERT INTO `permission` VALUES (61, 'personage:info', '个人权限合集', 'PAGE', '所有不归属于管理权限的则都为个人权限 或者区分俩者', '', '', '', 0, 0, 1, '2025-11-17 18:54:00', '2025-11-17 18:55:46');
INSERT INTO `permission` VALUES (62, 'admin:user:role', '用户角色分配', 'ACTION', '为用户分配角色，需要admin:user:role权限', '', '/api/admin/users/roles', 'POST', 23, 0, 1, '2025-11-17 22:28:36', '2025-11-17 22:28:36');

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '简介',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细内容（Markdown）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类：PACK, MOD, RESOURCE',
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '版本',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件URL',
  `thumbnail_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图URL',
  `download_count` int(11) NULL DEFAULT 0 COMMENT '下载次数',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` int(11) NULL DEFAULT 0 COMMENT '收藏数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, APPROVED, REJECTED',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES (6, '测试', '测试', '擦拭擦拭啊水水阿萨的啥的', 'MOD', '彩色', 2, NULL, NULL, 1, 0, 1, 'APPROVED', '2025-11-03 21:26:45', '2025-11-14 12:05:36');
INSERT INTO `resource` VALUES (7, '测试上传资源', '321321', '1233213211234了吧124吧42被吧b', 'RESOURCE', '11223', 5, NULL, NULL, 0, 0, 1, 'APPROVED', '2025-11-13 22:23:20', '2025-11-14 12:05:33');
INSERT INTO `resource` VALUES (8, '测试1', '测试1', '上传一下啊啊图标', 'PACK', '111', 5, NULL, NULL, 0, 0, 0, 'APPROVED', '2025-11-14 04:19:53', '2025-11-14 12:05:30');
INSERT INTO `resource` VALUES (9, '测试上传文件', '测试上传文件', '==>  Preparing: INSERT INTO sys_file ( original_name, file_name, file_url, file_size, file_type, create_user, update_user, create_time, update_time ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )\r\n==> Parameters: 抢关注.txt(String), 247494615693922304.txt(String), https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247494615693922304.txt(String), 755(Long), text/plain(String), 5(Long), 5(Long), 2025-11-14T06:55:15.180590100(LocalDateTime), 2025-11-14T06:55:15.180590100(LocalDateTime)\r\n<==    Updates: 1\r\nClosing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@597d1984]\r\n2025-11-14 06:55:15 [http-nio-8080-exec-6] INFO  com.minecraftforum.service.impl.FileServiceImpl - 文件上传成功: originalName=抢关注.txt, fileName=247494615693922304.txt, fileUrl=https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247494615693922304.txt\r\n2025-11-14 06:55:15 [http-nio-8080-exec-2] ERROR com.minecraftforum.common.GlobalExceptionHandler - 系统异常\r\norg.springframework.web.bind.MissingServletRequestParameterException: Required request parameter \'title\' for method parameter type String is not present', 'MOD', '111', 5, 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247495410921377792.txt', NULL, 2, 0, 0, 'APPROVED', '2025-11-14 06:58:25', '2025-11-14 12:05:29');
INSERT INTO `resource` VALUES (10, '测试资源上传', '测试资源上传', '测试资源上传测试资源上传测试资源上传测试资源上传', 'MOD', '111', 5, NULL, NULL, 7, 0, 1, 'APPROVED', '2025-11-14 07:08:49', '2025-11-14 12:05:27');
INSERT INTO `resource` VALUES (11, '测试多文件上传', '测试多文件上传', '测试多文件上传测试多文件上传测试多文件上传测试多文件上传测试多文件上传 测试非作者更新', 'PACK', '1111', 5, NULL, NULL, 7, 1, 1, 'APPROVED', '2025-11-14 08:42:19', '2025-11-15 00:37:57');
INSERT INTO `resource` VALUES (12, '测试发布的图片', '测试发布的图片', '![image.png](https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247529587314135040.png)\n测试修改', 'RESOURCE', '1111', 5, NULL, NULL, 3, 2, 2, 'APPROVED', '2025-11-14 09:15:30', '2025-11-14 12:05:23');
INSERT INTO `resource` VALUES (17, '测试新版发布资源', '测试新版发布资源测试新版发布资源', '测试新版发布资源测试新版发布资源测试新版发布资源测试新版发布资源', 'RESOURCE', '测试新版发布资源测试新版发布资源', 2, NULL, NULL, 0, 0, 0, 'APPROVED', '2025-11-14 12:21:24', '2025-11-15 00:41:19');
INSERT INTO `resource` VALUES (18, '测试新版发布资源测试新版发布源', '测试新版发布资源测试新版发布资源测试新版发布资源测试新版发布资源', '测试新版发布资源测试新版发布资源测试新版发布资源', 'MOD', '测试新版发布资源测试新版发布资源', 2, NULL, NULL, 7, 1, 1, 'APPROVED', '2025-11-14 12:22:35', '2025-11-16 15:25:52');

-- ----------------------------
-- Table structure for resource_tag
-- ----------------------------
DROP TABLE IF EXISTS `resource_tag`;
CREATE TABLE `resource_tag`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` bigint(20) NOT NULL COMMENT '资源ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_tag_name`(`tag_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource_tag
-- ----------------------------
INSERT INTO `resource_tag` VALUES (1, 2, '测试测试');
INSERT INTO `resource_tag` VALUES (2, 5, 'RPG');
INSERT INTO `resource_tag` VALUES (3, 5, 'PVP');
INSERT INTO `resource_tag` VALUES (4, 6, '彩色');
INSERT INTO `resource_tag` VALUES (5, 7, '创造');
INSERT INTO `resource_tag` VALUES (6, 7, '建筑');
INSERT INTO `resource_tag` VALUES (7, 8, '1');
INSERT INTO `resource_tag` VALUES (8, 8, '1 ');
INSERT INTO `resource_tag` VALUES (9, 8, 'PVP');
INSERT INTO `resource_tag` VALUES (10, 8, 'PVE');
INSERT INTO `resource_tag` VALUES (11, 8, '冒险');
INSERT INTO `resource_tag` VALUES (12, 8, '建筑');
INSERT INTO `resource_tag` VALUES (13, 9, '建筑');
INSERT INTO `resource_tag` VALUES (14, 9, '创造');
INSERT INTO `resource_tag` VALUES (15, 9, '生存');
INSERT INTO `resource_tag` VALUES (16, 9, 'PVP');
INSERT INTO `resource_tag` VALUES (17, 9, 'PVE');
INSERT INTO `resource_tag` VALUES (18, 9, 'RPG');
INSERT INTO `resource_tag` VALUES (19, 9, '冒险');
INSERT INTO `resource_tag` VALUES (20, 9, '红石');
INSERT INTO `resource_tag` VALUES (21, 9, '模组');
INSERT INTO `resource_tag` VALUES (22, 9, '插件');
INSERT INTO `resource_tag` VALUES (23, 10, '11');
INSERT INTO `resource_tag` VALUES (24, 11, '生存');
INSERT INTO `resource_tag` VALUES (25, 11, 'PVP');
INSERT INTO `resource_tag` VALUES (26, 11, 'RPG');
INSERT INTO `resource_tag` VALUES (27, 11, '建筑');
INSERT INTO `resource_tag` VALUES (28, 12, '创造');
INSERT INTO `resource_tag` VALUES (29, 12, '冒险');
INSERT INTO `resource_tag` VALUES (30, 12, '红石');
INSERT INTO `resource_tag` VALUES (31, 17, '建筑');
INSERT INTO `resource_tag` VALUES (32, 17, 'PVP');
INSERT INTO `resource_tag` VALUES (33, 18, '测试新版发布资源测试新版发布资源');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色代码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` int(11) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '普通用户', 'USER', '普通用户角色', 1, '2025-11-13 06:42:17', '2025-11-15 00:41:12');
INSERT INTO `role` VALUES (2, '管理员', 'ADMIN', '管理员角色', 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `role` VALUES (4, '测试角色', 'TESTROLE', '', 1, '2025-11-14 12:10:28', '2025-11-14 12:10:28');
INSERT INTO `role` VALUES (5, '测试新权限控制', 'TESTNEWROLE', '测试新权限控制', 1, '2025-11-16 10:59:05', '2025-11-16 10:59:05');

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限代码',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_code` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_code`(`permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 381 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 2, 'page:home', '访问首页', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (2, 2, 'page:forum', '访问论坛', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (4, 2, 'page:admin', '访问后台管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (5, 2, 'page:home:all', '访问首页-全部', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (6, 2, 'page:home:pack', '访问首页-整合包', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (7, 2, 'page:home:mod', '访问首页-MOD', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (8, 2, 'page:home:resource', '访问首页-资源包', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (23, 2, 'admin:user:manage', '用户管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (24, 2, 'admin:user:read', '查看用户列表', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (26, 2, 'admin:user:delete', '删除用户', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (27, 2, 'admin:resource:manage', '资源管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (28, 2, 'admin:post:manage', '帖子管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (30, 2, 'admin:permission:manage', '权限管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (31, 2, 'admin:role:manage', '角色管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (32, 2, 'admin:role:read', '查看角色', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (33, 2, 'admin:role:create', '创建角色', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (34, 2, 'admin:role:update', '修改角色', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (35, 2, 'admin:role:delete', '删除角色', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (105, 2, 'admin:category:manage', '分类管理', '2025-11-13 22:13:10');
INSERT INTO `role_permission` VALUES (241, 2, 'admin:permission:read', '获取所有权限列表', '2025-11-15 01:45:32');
INSERT INTO `role_permission` VALUES (242, 2, 'admin:permission:create', '添加权限', '2025-11-15 01:46:56');
INSERT INTO `role_permission` VALUES (243, 2, 'admin:permission:delete', '删除一个权限', '2025-11-15 01:48:02');
INSERT INTO `role_permission` VALUES (244, 2, 'admin:permission:update', '编辑权限', '2025-11-15 01:48:44');
INSERT INTO `role_permission` VALUES (245, 2, 'admin:category:read', '获取分类配置的数据', '2025-11-15 01:53:42');
INSERT INTO `role_permission` VALUES (246, 2, 'admin:category:update', '编辑分类', '2025-11-15 01:55:33');
INSERT INTO `role_permission` VALUES (247, 2, 'admin:category:create', '添加分类', '2025-11-15 01:55:33');
INSERT INTO `role_permission` VALUES (248, 2, 'admin:category:delete', '删除分类', '2025-11-15 01:55:33');
INSERT INTO `role_permission` VALUES (249, 2, 'admin:post:read', '获取帖子数据', '2025-11-15 01:57:40');
INSERT INTO `role_permission` VALUES (250, 2, 'admin:post:update', '修改帖子', '2025-11-15 01:58:53');
INSERT INTO `role_permission` VALUES (251, 2, 'admin:post:delete', '删除一篇帖子', '2025-11-15 02:00:07');
INSERT INTO `role_permission` VALUES (253, 2, 'admin:resource:read', '获取资源管理的数据', '2025-11-15 02:03:35');
INSERT INTO `role_permission` VALUES (254, 2, 'admin:resource:delete', '删除资源', '2025-11-15 02:04:21');
INSERT INTO `role_permission` VALUES (255, 2, 'admin:resource:update', '修改资源', '2025-11-15 02:04:37');
INSERT INTO `role_permission` VALUES (256, 2, 'admin:user:create', '创建用户', '2025-11-15 02:06:03');
INSERT INTO `role_permission` VALUES (257, 2, 'admin:post:create', '发布帖子', '2025-11-15 02:29:18');
INSERT INTO `role_permission` VALUES (259, 4, 'page:upload', '访问上传资源', '2025-11-15 02:53:38');
INSERT INTO `role_permission` VALUES (263, 4, 'page:home', '访问首页', '2025-11-15 02:53:38');
INSERT INTO `role_permission` VALUES (266, 5, 'page:upload', '访问上传资源', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (267, 5, 'admin:permission:manage', '权限管理', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (268, 5, 'page:admin', '访问后台管理', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (269, 5, 'page:home', '访问首页', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (270, 5, 'admin:resource:manage', '资源管理', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (271, 5, 'admin:post:manage', '帖子管理', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (272, 5, 'page:forum', '访问论坛', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (273, 5, 'admin:category:manage', '分类管理', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (274, 5, 'admin:role:manage', '角色管理', '2025-11-16 11:00:06');
INSERT INTO `role_permission` VALUES (275, 5, 'user:info', '获取当前用户的信息', '2025-11-16 11:31:56');
INSERT INTO `role_permission` VALUES (276, 1, 'user:info', '获取当前用户的信息', '2025-11-16 11:31:56');
INSERT INTO `role_permission` VALUES (278, 4, 'user:info', '获取当前用户的信息', '2025-11-16 11:31:56');
INSERT INTO `role_permission` VALUES (279, 4, 'admin:resource:read', '获取资源管理的数据', '2025-11-16 12:24:53');
INSERT INTO `role_permission` VALUES (280, 4, 'admin:post:read', '获取帖子数据', '2025-11-16 12:26:31');
INSERT INTO `role_permission` VALUES (281, 4, 'resource:read', '查看资源', '2025-11-16 12:26:49');
INSERT INTO `role_permission` VALUES (282, 4, 'page:home:pack', '访问首页-整合包', '2025-11-16 12:27:14');
INSERT INTO `role_permission` VALUES (283, 4, 'page:home:resource', '访问首页-资源包', '2025-11-16 12:27:14');
INSERT INTO `role_permission` VALUES (284, 4, 'page:home:mod', '访问首页-MOD', '2025-11-16 12:27:14');
INSERT INTO `role_permission` VALUES (285, 4, 'page:home:all', '访问首页-全部', '2025-11-16 12:27:14');
INSERT INTO `role_permission` VALUES (286, 4, 'page:admin', '访问后台管理', '2025-11-16 12:27:35');
INSERT INTO `role_permission` VALUES (287, 4, 'admin:user:manage', '后台管理-用户管理', '2025-11-16 12:29:21');
INSERT INTO `role_permission` VALUES (288, 4, 'admin:user:read', '查看用户列表', '2025-11-16 12:29:35');
INSERT INTO `role_permission` VALUES (289, 4, 'admin:user:create', '创建用户', '2025-11-16 12:29:40');
INSERT INTO `role_permission` VALUES (290, 4, 'admin:user:update', '修改用户', '2025-11-16 12:29:44');
INSERT INTO `role_permission` VALUES (291, 4, 'admin:user:delete', '删除用户', '2025-11-16 12:29:52');
INSERT INTO `role_permission` VALUES (292, 4, 'admin:role:read', '查看角色', '2025-11-16 12:30:28');
INSERT INTO `role_permission` VALUES (293, 4, 'admin:role:manage', '后台管理-角色管理', '2025-11-16 12:30:28');
INSERT INTO `role_permission` VALUES (294, 4, 'admin:role:create', '创建角色', '2025-11-16 12:30:46');
INSERT INTO `role_permission` VALUES (295, 4, 'admin:role:update', '修改角色', '2025-11-16 12:30:46');
INSERT INTO `role_permission` VALUES (296, 4, 'admin:role:delete', '删除角色', '2025-11-16 12:30:46');
INSERT INTO `role_permission` VALUES (298, 2, 'admin:file:manage', '后台管理-文件管理', '2025-11-17 04:58:24');
INSERT INTO `role_permission` VALUES (299, 2, 'admin:file:read', '获取文件列表', '2025-11-17 05:03:14');
INSERT INTO `role_permission` VALUES (304, 1, 'resource:download', '下载文件', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (305, 1, 'comment:create', '创建评论', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (306, 1, 'resource:delete', '删除资源', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (307, 1, 'post:read', '查看帖子', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (308, 1, 'elasticsearch:search', '分词搜索', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (309, 1, 'post:create', '创建帖子', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (310, 1, 'comment:update', '修改评论', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (311, 1, 'post:update', '修改帖子', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (312, 1, 'comment:delete', '删除评论', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (313, 1, 'post:delete', '删除帖子', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (314, 1, 'resource:read', '查看资源', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (315, 1, 'resource:update', '修改资源', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (316, 1, 'personage:info', '个人权限合集', '2025-11-17 19:08:41');
INSERT INTO `role_permission` VALUES (318, 4, 'admin:resource:manage', '后台管理-资源管理', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (319, 4, 'elasticsearch:search', '分词搜索', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (320, 4, 'personage:info', '个人权限合集', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (321, 4, 'comment:create', '创建评论', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (322, 4, 'resource:delete', '删除资源', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (323, 4, 'post:read', '查看帖子', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (324, 4, 'post:create', '创建帖子', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (325, 4, 'comment:update', '修改评论', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (326, 4, 'post:update', '修改帖子', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (327, 4, 'comment:delete', '删除评论', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (328, 4, 'post:delete', '删除帖子', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (329, 4, 'resource:update', '修改资源', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (330, 4, 'admin:post:manage', '后台管理-帖子管理', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (331, 4, 'page:forum', '访问论坛', '2025-11-17 19:08:49');
INSERT INTO `role_permission` VALUES (332, 5, 'resource:download', '下载文件', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (333, 5, 'comment:create', '创建评论', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (334, 5, 'resource:delete', '删除资源', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (335, 5, 'post:read', '查看帖子', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (336, 5, 'elasticsearch:search', '分词搜索', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (337, 5, 'post:create', '创建帖子', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (338, 5, 'comment:update', '修改评论', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (339, 5, 'post:update', '修改帖子', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (340, 5, 'comment:delete', '删除评论', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (341, 5, 'post:delete', '删除帖子', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (342, 5, 'resource:read', '查看资源', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (343, 5, 'resource:update', '修改资源', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (344, 5, 'personage:info', '个人权限合集', '2025-11-17 19:08:56');
INSERT INTO `role_permission` VALUES (345, 2, 'user:info', '获取当前用户的信息', '2025-11-17 19:13:51');
INSERT INTO `role_permission` VALUES (346, 2, 'personage:info', '个人权限合集', '2025-11-17 19:13:51');
INSERT INTO `role_permission` VALUES (359, 2, 'resource:download', '下载文件', '2025-11-17 19:27:44');
INSERT INTO `role_permission` VALUES (360, 2, 'comment:delete', '删除评论', '2025-11-17 19:27:44');
INSERT INTO `role_permission` VALUES (361, 2, 'elasticsearch:search', '分词搜索', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (362, 2, 'resource:delete', '删除资源', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (363, 2, 'post:create', '创建帖子', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (364, 2, 'post:update', '修改帖子', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (365, 2, 'resource:read', '查看资源', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (366, 2, 'comment:create', '创建评论', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (367, 2, 'post:read', '查看帖子', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (368, 2, 'comment:update', '修改评论', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (369, 2, 'post:delete', '删除帖子', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (370, 2, 'resource:update', '修改资源', '2025-11-17 19:27:58');
INSERT INTO `role_permission` VALUES (373, 2, 'admin:file:delete', '删除文件', '2025-11-17 20:55:48');
INSERT INTO `role_permission` VALUES (374, 2, 'admin:resource:audit', '审核资源', '2025-11-17 22:23:55');
INSERT INTO `role_permission` VALUES (375, 2, 'admin:user:update', '修改用户', '2025-11-17 22:23:55');
INSERT INTO `role_permission` VALUES (378, 2, 'admin:user:role', '用户角色分配', '2025-11-17 22:58:42');
INSERT INTO `role_permission` VALUES (379, 2, 'page:upload', '访问上传资源', '2025-11-17 23:05:24');
INSERT INTO `role_permission` VALUES (380, 2, 'resource:create', '创建资源', '2025-11-17 23:05:24');

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` bigint(20) NULL DEFAULT NULL COMMENT '所属资源ID（可选，用于关联资源）',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '雪花算法生成的文件名',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上传到 OSS 的访问 URL',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（单位：字节）',
  `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型（例如：image/png）',
  `create_user` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) NULL DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_create_user`(`create_user` ASC) USING BTREE,
  INDEX `idx_file_name`(`file_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file
-- ----------------------------
INSERT INTO `sys_file` VALUES (2, 9, '抢关注.txt', '247495410921377792.txt', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/2474915410921377792.txt', 755, 'text/plain', 5, 5, '2025-11-14 06:58:25', '2025-11-17 04:56:58');
INSERT INTO `sys_file` VALUES (3, 10, '抢关注.txt', '247498030738182144.txt', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/2474198030738182144.txt', 755, 'text/plain', 5, 5, '2025-11-14 07:08:49', '2025-11-17 04:57:00');
INSERT INTO `sys_file` VALUES (4, 11, '抢关注.txt', '247521564927594496.txt', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247521564927594496.txt', 755, 'text/plain', 5, 5, '2025-11-14 08:42:21', '2025-11-14 08:42:21');
INSERT INTO `sys_file` VALUES (5, 11, 'app.so', '247521568232706048.so', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247521568232706048.so', 7668640, 'application/octet-stream', 5, 5, '2025-11-14 08:42:22', '2025-11-14 08:42:22');
INSERT INTO `sys_file` VALUES (7, 12, 'mrzh_250918213951_449.jpg', '247529914272714752.jpg', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247529914272714752.jpg', 701953, 'image/jpeg', 5, 5, '2025-11-14 09:15:31', '2025-11-14 09:15:31');
INSERT INTO `sys_file` VALUES (12, 12, 'mrzh_250918213951_449.jpg', '247567512101851136.jpg', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247567512101851136.jpg', 701953, 'image/jpeg', 5, 5, '2025-11-14 11:44:55', '2025-11-14 11:44:55');
INSERT INTO `sys_file` VALUES (13, 12, 'mrzh_250918213951_449.jpg', '247568276580864000.jpg', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247568276580864000.jpg', 701953, 'image/jpeg', 5, 5, '2025-11-14 11:47:57', '2025-11-14 11:47:57');
INSERT INTO `sys_file` VALUES (18, 17, 'mrzh_250918213951_449.jpg', '247576694150533120.jpg', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247576694150533120.jpg', 701953, 'image/jpeg', 2, 2, '2025-11-14 12:21:24', '2025-11-14 12:21:24');
INSERT INTO `sys_file` VALUES (19, 18, 'mrzh_250918213951_449.jpg', '247576993296683008.jpg', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247576993296683008.jpg', 701953, 'image/jpeg', 2, 2, '2025-11-14 12:22:36', '2025-11-14 12:22:36');
INSERT INTO `sys_file` VALUES (20, 18, 'mrzh_250918213951_449.jpg', '247577306875432960.jpg', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247577306875432960.jpg', 701953, 'image/jpeg', 2, 2, '2025-11-14 12:23:50', '2025-11-14 12:23:50');
INSERT INTO `sys_file` VALUES (21, 18, 'mrzh_250918213951_449.jpg', '247763091343937536.jpg', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/files/247763091343937536.jpg', 701953, 'image/jpeg', 5, 5, '2025-11-15 00:42:05', '2025-11-15 00:42:05');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` int(11) NULL DEFAULT 0 COMMENT '状态：0-正常，1-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2, 'admin', '$2a$10$MJzs2BhCv20zspZyhfJmce5SPrx5lErB1ojCHTM.bIPV1B8jW7DcO', '安安安安', '1066863570@qq.com', 'https://minecraft-forum.oss-cn-shenzhen.aliyuncs.com/avatar/247578013976367104.jpg', 0, '2025-11-13 06:17:31', '2025-11-14 22:50:10');
INSERT INTO `user` VALUES (5, '测试1', '$2a$10$.i06GsDKs0sA1V02r2Dmgebc7QOUjETYziP/Bh4bLmbqbyUXEK2Ae', '测试用户', 'aa@q.com', NULL, 0, '2025-11-13 06:20:18', '2025-11-15 01:19:58');
INSERT INTO `user` VALUES (6, '测试新权限控制', '$2a$10$0eNjwyZUNgoj9WQp5LRHlOmlC89XHMhgV9n5OVnUmLmSsxrbvbSoa', '123456', 'chen_a_nan1@qq.com', NULL, 0, '2025-11-16 10:58:35', '2025-11-16 10:58:35');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 2, 2, '2025-11-13 06:44:05');
INSERT INTO `user_role` VALUES (7, 6, 5, '2025-11-16 11:01:10');
INSERT INTO `user_role` VALUES (8, 5, 2, '2025-11-17 22:52:23');
INSERT INTO `user_role` VALUES (9, 5, 5, '2025-11-17 22:58:14');

SET FOREIGN_KEY_CHECKS = 1;
