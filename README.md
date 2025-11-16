# Minecraft 论坛后端项目

基于 Spring Boot 3 的后端项目

> **前端项目地址**：  https://github.com/ANANAkiX/minecraft-forum-ui.git
> **前端开发服务器**：http://localhost:5173

## 技术栈

- Spring Boot 3
- MyBatis Plus
- MySQL 8
- Redis 7
- Spring Security + JWT
- Lombok
- Knife4j (Swagger3)

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8+
- Redis 7+

## 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE minecraft_forum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行 `src/main/resources/db/all_db.sql` 创建表结构以及数据

3. 修改 `application.yml` 中的数据库连接配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/minecraft_forum?allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

## Redis 配置

修改 `application.yml` 中的 Redis 连接配置：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果有密码，填写密码
      database: 0
```

## 文件上传配置

修改 `application.yml` 中的文件上传路径：
```yaml
file:
  upload-path: D:/minecraft-forum/uploads  # 根据实际情况修改路径
  access-path: /uploads/**
```

## 运行

1. 启动 MySQL 和 Redis 服务

2. 修改 `application.yml` 配置文件：
   - 数据库连接信息
   - Redis 连接信息
   - 文件上传路径
   - JWT Secret（生产环境必须修改）

3. 运行项目：
```bash
mvn spring-boot:run
```

或者使用 IDE 直接运行 `MinecraftForumApplication.java`

4. 后端服务启动后，默认运行在：http://localhost:8080

## API 文档

启动项目后访问：http://localhost:8080/doc.html

## 功能模块

### 用户模块
- 用户注册/登录（JWT 认证）
- 获取用户信息
- 修改用户资料
- 上传头像
- 用户角色管理

### 资源模块
- 资源列表（支持分类、搜索、分页）
- 资源详情
- 资源上传（支持 Markdown 编辑）
- 资源点赞/收藏
- 资源下载
- 资源审核

### 论坛模块
- 帖子列表（支持分类、搜索、分页）
- 帖子详情
- 发帖
- 评论/回复
- 点赞功能
- 热门帖子

### 后台管理
- 用户管理（CRUD、角色分配、状态管理）
- 资源管理（审核、编辑、删除）
- 帖子管理（编辑、删除）
- 角色管理（CRUD、权限分配）
- 权限管理（CRUD、API 扫描、路由配置）
- 分类管理（CRUD）

## API 接口规范

### 接口前缀
- 所有 API 接口统一使用 `/api` 前缀
- 示例：`/api/user/login`、`/api/forum/posts`

### 统一返回格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 认证方式
使用 JWT Token，在请求头中携带：
```
Authorization: Bearer <token>
```

### 权限控制
- 使用 Spring Security 进行权限验证
- 权限标识格式：`admin:模块:操作`（如：`admin:user:read`、`admin:post:create`）
- 支持页面访问权限（router）和操作权限（apiurl + methodtype）

## 项目结构

```
src/main/java/com/minecraftforum/
├── common/          # 通用类（Result、异常处理等）
├── config/          # 配置类（Security、CORS、Swagger等）
├── controller/      # 控制器（API 接口）
├── dto/             # 数据传输对象
├── entity/          # 实体类（数据库映射）
├── mapper/          # Mapper 接口（MyBatis）
├── security/        # 安全配置（JWT、权限验证）
├── service/         # 服务层（业务逻辑）
├── util/            # 工具类（ApiScanner、文件上传等）
└── MinecraftForumApplication.java  # 启动类
```

## 核心功能说明

### API 扫描功能
- `ApiScanner` 工具类可以自动扫描 `com.minecraftforum.controller` 包下的所有 Controller
- 自动提取 API 路径、请求方式、接口描述等信息
- 用于权限管理中的 API 选择器

### 权限系统
- 支持页面访问权限（PAGE）和操作权限（ACTION）
- 页面权限通过 `router` 字段配置前端路由
- 操作权限通过 `apiurl` 和 `methodtype` 字段配置后端 API
- 支持权限的层级结构（通过 `parentId` 实现）

### 文件上传
- 支持本地存储和阿里云 OSS 存储
- 配置文件路径在 `application.yml` 中
- 支持图片、文档等多种文件类型

## 注意事项

1. **生产环境配置**：
   - 必须修改 JWT Secret 为更安全的密钥
   - 修改数据库和 Redis 连接信息
   - 配置正确的文件上传路径或 OSS 配置

2. **CORS 配置**：
   - 开发环境已配置允许前端跨域访问
   - 生产环境需要根据实际情况调整 CORS 配置

3. **日志配置**：
   - 开发环境默认输出 DEBUG 级别日志
   - 生产环境建议调整为 INFO 或 WARN 级别

4. **数据库**：
   - 确保 MySQL 字符集为 `utf8mb4`
   - 确保时区配置正确（Asia/Shanghai）

5. **Redis**：
   - 确保 Redis 服务正常运行
   - 如果 Redis 有密码，需要在配置文件中填写

## License

MIT
