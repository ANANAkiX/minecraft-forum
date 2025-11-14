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

 Date: 13/11/2025 22:25:16
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
INSERT INTO `category_config` VALUES (10, '分享', 'SHARE', 'FORUM', 1, 0, 1, '2025-11-13 05:29:55', '2025-11-13 05:29:55');
INSERT INTO `category_config` VALUES (11, '求助', 'HELP', 'FORUM', 2, 0, 1, '2025-11-13 05:29:55', '2025-11-13 05:29:55');
INSERT INTO `category_config` VALUES (12, '教程', 'TUTORIAL', 'FORUM', 3, 0, 1, '2025-11-13 05:29:55', '2025-11-13 05:29:55');
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 1, 2, '彩色彩色', 0, '2025-11-03 02:29:24');
INSERT INTO `comment` VALUES (2, 2, 2, '测试', 0, '2025-11-03 02:29:42');
INSERT INTO `comment` VALUES (3, 2, 2, '啊', 0, '2025-11-03 02:31:04');

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '下载日志表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (3, 2, 2, '2025-11-03 02:19:14');
INSERT INTO `favorite` VALUES (4, 2, 6, '2025-11-13 05:15:55');

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '论坛帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post
-- ----------------------------
INSERT INTO `forum_post` VALUES (1, '测试测试', '测试乘势而上', 'TUTORIAL', 2, 8, 0, 1, 'DELETED', '2025-11-03 02:28:56', '2025-11-03 02:28:56');
INSERT INTO `forum_post` VALUES (2, '彩色', '彩色是', 'HELP', 2, 9, 0, 2, 'DELETED', '2025-11-03 02:29:39', '2025-11-03 02:29:39');

-- ----------------------------
-- Table structure for forum_reply
-- ----------------------------
DROP TABLE IF EXISTS `forum_reply`;
CREATE TABLE `forum_reply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
  `target_user_id` bigint(20) NULL DEFAULT NULL COMMENT '目标用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回复表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_reply
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of like
-- ----------------------------

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
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父权限ID，0表示顶级权限',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `status` int(11) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, 'page:home', '访问首页', 'PAGE', '访问首页', 0, 1, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (2, 'page:forum', '访问论坛', 'PAGE', '访问论坛页面', 0, 2, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (3, 'page:upload', '访问上传资源', 'PAGE', '访问上传资源页面', 0, 3, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (4, 'page:admin', '访问后台管理', 'PAGE', '访问后台管理页面', 0, 4, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (5, 'page:home:all', '访问首页-全部', 'PAGE', '访问首页全部分类', 0, 5, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (6, 'page:home:pack', '访问首页-整合包', 'PAGE', '访问首页整合包分类', 0, 6, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (7, 'page:home:mod', '访问首页-MOD', 'PAGE', '访问首页MOD分类', 0, 7, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (8, 'page:home:resource', '访问首页-资源包', 'PAGE', '访问首页资源包分类', 0, 8, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (9, 'resource:create', '创建资源', 'ACTION', '创建/上传资源', 0, 10, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (10, 'resource:read', '查看资源', 'ACTION', '查看资源详情', 0, 11, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (11, 'resource:update', '修改资源', 'ACTION', '修改资源', 0, 12, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (12, 'resource:delete', '删除资源', 'ACTION', '删除资源', 0, 13, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (13, 'post:create', '创建帖子', 'ACTION', '发布帖子', 0, 20, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (14, 'post:read', '查看帖子', 'ACTION', '查看帖子详情', 0, 21, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (15, 'post:update', '修改帖子', 'ACTION', '修改帖子', 0, 22, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (16, 'post:delete', '删除帖子', 'ACTION', '删除帖子', 0, 23, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (17, 'comment:create', '创建评论', 'ACTION', '发表评论', 0, 30, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (18, 'comment:update', '修改评论', 'ACTION', '修改评论', 0, 31, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (19, 'comment:delete', '删除评论', 'ACTION', '删除评论', 0, 32, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (20, 'user:read', '查看用户', 'ACTION', '查看用户信息', 0, 40, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (21, 'user:update', '修改用户', 'ACTION', '修改用户信息', 0, 41, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (22, 'user:delete', '删除用户', 'ACTION', '删除用户', 0, 42, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (23, 'admin:user:manage', '用户管理', 'ACTION', '管理用户（包含所有用户管理操作）', 0, 50, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (24, 'admin:user:read', '查看用户列表', 'ACTION', '查看用户列表', 0, 51, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (25, 'admin:user:update', '修改用户', 'ACTION', '修改用户信息', 0, 52, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (26, 'admin:user:delete', '删除用户', 'ACTION', '删除用户', 0, 53, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (27, 'admin:resource:manage', '资源管理', 'ACTION', '管理资源', 0, 54, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (28, 'admin:post:manage', '帖子管理', 'ACTION', '管理帖子', 0, 55, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (29, 'admin:category:manage', '分类管理', 'ACTION', '管理分类配置', 0, 56, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (30, 'admin:permission:manage', '权限管理', 'ACTION', '管理权限', 0, 57, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (31, 'admin:role:manage', '角色管理', 'ACTION', '管理角色（创建、编辑、删除角色）', 0, 58, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (32, 'admin:role:read', '查看角色', 'ACTION', '查看角色列表', 0, 59, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (33, 'admin:role:create', '创建角色', 'ACTION', '创建新角色', 0, 60, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (34, 'admin:role:update', '修改角色', 'ACTION', '修改角色信息', 0, 61, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `permission` VALUES (35, 'admin:role:delete', '删除角色', 'ACTION', '删除角色', 0, 62, 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES (6, '测试', '测试', '擦拭擦拭啊水水阿萨的啥的', 'MOD', '彩色', 2, NULL, NULL, 1, 0, 1, 'APPROVED', '2025-11-03 21:26:45', '2025-11-03 21:26:45');
INSERT INTO `resource` VALUES (7, '测试上传资源', '321321', '1233213211234了吧124吧42被吧b', 'MOD', '11223', 5, NULL, NULL, 0, 0, 0, 'APPROVED', '2025-11-13 22:23:20', '2025-11-13 22:23:20');

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
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource_tag
-- ----------------------------
INSERT INTO `resource_tag` VALUES (1, 2, '测试测试');
INSERT INTO `resource_tag` VALUES (2, 5, 'RPG');
INSERT INTO `resource_tag` VALUES (3, 5, 'PVP');
INSERT INTO `resource_tag` VALUES (4, 6, '彩色');
INSERT INTO `resource_tag` VALUES (5, 7, '创造');
INSERT INTO `resource_tag` VALUES (6, 7, '建筑');

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '普通用户', 'USER', '普通用户角色', 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');
INSERT INTO `role` VALUES (2, '管理员', 'ADMIN', '管理员角色', 1, '2025-11-13 06:42:17', '2025-11-13 06:42:17');

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
) ENGINE = InnoDB AUTO_INCREMENT = 148 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 2, 'page:home', '访问首页', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (2, 2, 'page:forum', '访问论坛', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (3, 2, 'page:upload', '访问上传资源', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (4, 2, 'page:admin', '访问后台管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (5, 2, 'page:home:all', '访问首页-全部', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (6, 2, 'page:home:pack', '访问首页-整合包', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (7, 2, 'page:home:mod', '访问首页-MOD', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (8, 2, 'page:home:resource', '访问首页-资源包', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (9, 2, 'resource:create', '创建资源', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (10, 2, 'resource:read', '查看资源', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (11, 2, 'resource:update', '修改资源', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (12, 2, 'resource:delete', '删除资源', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (13, 2, 'post:create', '创建帖子', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (14, 2, 'post:read', '查看帖子', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (15, 2, 'post:update', '修改帖子', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (16, 2, 'post:delete', '删除帖子', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (17, 2, 'comment:create', '创建评论', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (18, 2, 'comment:update', '修改评论', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (19, 2, 'comment:delete', '删除评论', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (20, 2, 'user:read', '查看用户', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (21, 2, 'user:update', '修改用户', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (22, 2, 'user:delete', '删除用户', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (23, 2, 'admin:user:manage', '用户管理', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (24, 2, 'admin:user:read', '查看用户列表', '2025-11-13 06:42:17');
INSERT INTO `role_permission` VALUES (25, 2, 'admin:user:update', '修改用户', '2025-11-13 06:42:17');
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
INSERT INTO `role_permission` VALUES (112, 1, 'page:home', '访问首页', '2025-11-13 22:17:26');
INSERT INTO `role_permission` VALUES (113, 1, 'page:forum', '访问论坛', '2025-11-13 22:17:26');
INSERT INTO `role_permission` VALUES (114, 1, 'page:upload', '访问上传资源', '2025-11-13 22:17:27');
INSERT INTO `role_permission` VALUES (115, 1, 'page:home:all', '访问首页-全部', '2025-11-13 22:17:27');
INSERT INTO `role_permission` VALUES (116, 1, 'page:home:pack', '访问首页-整合包', '2025-11-13 22:17:27');
INSERT INTO `role_permission` VALUES (117, 1, 'page:home:mod', '访问首页-MOD', '2025-11-13 22:17:27');
INSERT INTO `role_permission` VALUES (118, 1, 'page:home:resource', '访问首页-资源包', '2025-11-13 22:17:27');
INSERT INTO `role_permission` VALUES (119, 1, 'page:admin', '访问后台管理', '2025-11-13 22:17:33');
INSERT INTO `role_permission` VALUES (120, 1, 'admin:category:manage', '分类管理', '2025-11-13 22:17:43');
INSERT INTO `role_permission` VALUES (122, 1, 'admin:role:read', '查看角色', '2025-11-13 22:18:55');
INSERT INTO `role_permission` VALUES (123, 1, 'admin:role:update', '修改角色', '2025-11-13 22:19:38');
INSERT INTO `role_permission` VALUES (124, 1, 'admin:role:delete', '删除角色', '2025-11-13 22:19:47');
INSERT INTO `role_permission` VALUES (125, 1, 'admin:role:create', '创建角色', '2025-11-13 22:19:51');
INSERT INTO `role_permission` VALUES (126, 1, 'admin:role:manage', '角色管理', '2025-11-13 22:20:46');
INSERT INTO `role_permission` VALUES (127, 1, 'admin:permission:manage', '权限管理', '2025-11-13 22:21:40');
INSERT INTO `role_permission` VALUES (128, 1, 'admin:post:manage', '帖子管理', '2025-11-13 22:22:03');
INSERT INTO `role_permission` VALUES (129, 1, 'admin:resource:manage', '资源管理', '2025-11-13 22:22:09');
INSERT INTO `role_permission` VALUES (130, 1, 'admin:user:read', '查看用户列表', '2025-11-13 22:22:17');
INSERT INTO `role_permission` VALUES (131, 1, 'admin:user:update', '修改用户', '2025-11-13 22:23:38');
INSERT INTO `role_permission` VALUES (132, 1, 'admin:user:delete', '删除用户', '2025-11-13 22:23:44');
INSERT INTO `role_permission` VALUES (133, 1, 'admin:user:manage', '用户管理', '2025-11-13 22:23:58');
INSERT INTO `role_permission` VALUES (134, 1, 'user:delete', '删除用户', '2025-11-13 22:24:05');
INSERT INTO `role_permission` VALUES (135, 1, 'user:update', '修改用户', '2025-11-13 22:24:05');
INSERT INTO `role_permission` VALUES (136, 1, 'user:read', '查看用户', '2025-11-13 22:24:06');
INSERT INTO `role_permission` VALUES (137, 1, 'comment:delete', '删除评论', '2025-11-13 22:24:21');
INSERT INTO `role_permission` VALUES (138, 1, 'comment:update', '修改评论', '2025-11-13 22:24:21');
INSERT INTO `role_permission` VALUES (139, 1, 'comment:create', '创建评论', '2025-11-13 22:24:22');
INSERT INTO `role_permission` VALUES (140, 1, 'post:delete', '删除帖子', '2025-11-13 22:24:22');
INSERT INTO `role_permission` VALUES (141, 1, 'post:update', '修改帖子', '2025-11-13 22:24:22');
INSERT INTO `role_permission` VALUES (142, 1, 'post:read', '查看帖子', '2025-11-13 22:24:22');
INSERT INTO `role_permission` VALUES (143, 1, 'resource:create', '创建资源', '2025-11-13 22:24:22');
INSERT INTO `role_permission` VALUES (144, 1, 'resource:read', '查看资源', '2025-11-13 22:24:22');
INSERT INTO `role_permission` VALUES (145, 1, 'resource:update', '修改资源', '2025-11-13 22:24:23');
INSERT INTO `role_permission` VALUES (146, 1, 'resource:delete', '删除资源', '2025-11-13 22:24:23');
INSERT INTO `role_permission` VALUES (147, 1, 'post:create', '创建帖子', '2025-11-13 22:24:23');

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
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'USER' COMMENT '角色：USER, ADMIN',
  `status` int(11) NULL DEFAULT 0 COMMENT '状态：0-正常，1-禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2, 'admin', '$2a$10$MJzs2BhCv20zspZyhfJmce5SPrx5lErB1ojCHTM.bIPV1B8jW7DcO', '安安', '1066863570@qq.com', NULL, 'ADMIN', 0, '2025-11-13 06:17:31', '2025-11-13 06:17:31');
INSERT INTO `user` VALUES (5, '测试1', '$2a$10$.i06GsDKs0sA1V02r2Dmgebc7QOUjETYziP/Bh4bLmbqbyUXEK2Ae', '测试1', 'aa@q.com', NULL, 'USER', 0, '2025-11-13 06:20:18', '2025-11-13 22:15:50');

-- ----------------------------
-- Table structure for user_permission
-- ----------------------------
DROP TABLE IF EXISTS `user_permission`;
CREATE TABLE `user_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_permission`(`user_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_permission
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 2, 2, '2025-11-13 06:44:05');
INSERT INTO `user_role` VALUES (2, 2, 1, '2025-11-13 21:57:48');
INSERT INTO `user_role` VALUES (3, 5, 1, '2025-11-13 22:15:26');

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` bigint(20) DEFAULT NULL COMMENT '所属资源ID（可选，用于关联资源）',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '雪花算法生成的文件名',
  `file_url` varchar(500) NOT NULL COMMENT '上传到 OSS 的访问 URL',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小（单位：字节）',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型（例如：image/png）',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_resource_id` (`resource_id`) USING BTREE,
  KEY `idx_create_user` (`create_user`) USING BTREE,
  KEY `idx_file_name` (`file_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统文件表';

SET FOREIGN_KEY_CHECKS = 1;
