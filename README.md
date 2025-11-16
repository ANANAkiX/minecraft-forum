# Minecraft 论坛后端项目

基于 Spring Boot 3 的后端项目

> **前端项目地址**：  https://github.com/ANANAkiX/minecraft-forum-ui.git
> 
> **前端开发服务器**：http://localhost:5173

## 技术栈

- Spring Boot 3
- MyBatis Plus
- MySQL 8
- Redis 7（Token 存储、API 缓存）
- Elasticsearch 8+（全文搜索，可选）
- Spring Security + UUID Token（基于 Redis）
- Spring AOP（异步索引）
- Spring Events（事件驱动权限同步）
- Lombok
- Knife4j (Swagger3)

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8+
- Redis 7+
- Elasticsearch 8+（可选，用于全文搜索功能）

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

**Redis 用途**：
- Token 存储（UUID 与用户信息的映射）
- API 扫描结果缓存
- 其他缓存数据

## Elasticsearch 配置

修改 `application.yml` 中的 Elasticsearch 连接配置（可选）：
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200  # Elasticsearch 服务器地址
    scheme: http  # 协议：http 或 https
    connection-timeout: 5s
    socket-timeout: 60s
```

**Elasticsearch 特性**：
- 连接失败不影响主程序运行
- 自动健康检查和重连机制
- 应用启动后延迟 5 秒检查连接
- 每 30 秒自动检查连接状态
- 搜索服务不可用时返回 503 错误

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
   - Elasticsearch 连接信息（可选）
   - 文件上传路径
   - JWT Secret（生产环境必须修改）
   - Token 配置（多点登录控制）
   - API 扫描配置（启动时是否扫描）
   - CORS 配置（跨域设置）

3. 运行项目：
```bash
mvn spring-boot:run
```

或者使用 IDE 直接运行 `MinecraftForumApplication.java`

4. 后端服务启动后，默认运行在：http://localhost:8080

### 登录账号 admin
### 登录密码 123456

## API 文档

启动项目后访问：http://localhost:8080/doc.html

## 功能模块

### 用户模块
- 用户注册/登录（UUID Token 认证，基于 Redis）
- 获取用户信息（自动清除密码信息）
- 修改用户资料
- 上传头像（支持本地存储和阿里云 OSS）
- 管理员创建用户
- 用户角色管理（通过 user_role 表关联）

### 资源模块
- 资源列表（支持分类、搜索、分页）
- 资源详情
- 资源上传（支持 Markdown 编辑、多文件上传）
- 资源点赞/收藏
- 资源下载
- 资源审核（管理员）
- 自动索引到 Elasticsearch（创建、更新、删除时）

### 论坛模块
- 帖子列表（支持分类、搜索、分页）
- 帖子详情
- 发帖（支持 Markdown 编辑）
- 评论/回复（支持无限层级嵌套）
- 点赞功能
- 热门帖子展示
- 自动索引到 Elasticsearch（创建、更新、删除时）

### 搜索模块
- Elasticsearch 全文搜索
- 搜索帖子和资源（包括资源文件名）
- 搜索结果高亮显示（支持夜间模式）
- 自动索引（使用 AOP 切面异步处理）
- 健康检查和自动重连
- 服务不可用时返回友好提示（503 错误）

### 后台管理
- **用户管理**：用户列表、创建、编辑、删除、角色分配（通过"角色管理"按钮）
- **资源管理**：资源列表、审核、编辑、删除
- **帖子管理**：帖子列表、编辑、删除
- **角色管理**：角色列表、创建、编辑、删除、权限分配（事件驱动同步）
- **权限管理**：权限列表、创建、编辑、删除、API 扫描、路由配置、API 选择器
- **分类管理**：分类列表、创建、编辑、删除
- **文件管理**：文件列表、删除

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
使用 UUID Token（基于 Redis），在请求头中携带：
```
Authorization: Bearer <UUID>
```

**Token 特性**：
- Token 为 UUID 格式，存储在 Redis 中
- 包含用户 ID、用户名、权限列表等信息
- 支持多点登录控制（`jwt.allow-multiple-login` 配置）
- 权限更新时自动同步所有相关 Token（事件驱动）
- Token 刷新时更新权限但不改变 UUID

### 权限控制
- **统一拦截器**：`LoginInterceptor` 检查登录状态，`PermissionInterceptor` 检查权限
- **动态权限判断**：根据请求方法（GET/POST/PUT/DELETE）和 URL 从数据库查询权限
- **权限标识格式**：`模块:操作`（如：`admin:user:read`、`admin:post:create`、`elasticsearch:search`）
- **权限类型**：
  - 页面访问权限（PAGE）：通过 `router` 字段配置前端路由
  - 操作权限（ACTION）：通过 `apiurl` 和 `methodtype` 字段配置后端 API
- **匿名访问**：支持 `@AnonymousAccess` 注解标记无需登录的接口
- **权限同步**：使用 Spring Events 机制，权限更新时异步同步所有相关用户的 Token

## 项目结构

```
src/main/java/com/minecraftforum/
├── aspect/          # AOP 切面（Elasticsearch 异步索引）
├── common/          # 通用类（Result、异常处理等）
├── config/          # 配置类（Security、CORS、Swagger、Elasticsearch等）
│   ├── custom/      # 自定义配置（注解等）
│   └── ElasticsearchHealthChecker.java  # Elasticsearch 健康检查
├── controller/      # 控制器（API 接口）
├── dto/             # 数据传输对象
├── entity/          # 实体类（数据库映射）
│   └── es/          # Elasticsearch 实体类
├── event/           # Spring 事件（权限更新事件）
├── interceptor/     # 拦截器（登录、权限检查）
├── listener/        # 事件监听器（权限同步监听器）
├── mapper/          # Mapper 接口（MyBatis）
├── repository/      # Repository 接口（Elasticsearch）
├── security/        # 安全配置（JWT、权限验证）
├── service/         # 服务层（业务逻辑）
│   └── impl/        # 服务实现类
├── util/            # 工具类（ApiScanner、TokenUtil、文件上传等）
└── MinecraftForumApplication.java  # 启动类
```

## 核心功能说明

### API 扫描功能
- `ApiScanner` 工具类可以自动扫描 `com.minecraftforum.controller` 包下的所有 Controller
- 自动提取 API 路径、请求方式、接口描述（`@Operation` 注解）等信息
- 扫描结果缓存在 Redis 中，提高性能
- 支持配置启动时扫描或懒加载（`api-scanner.load-on-startup`）
- 支持排除特定 Controller 或方法（`api-scanner.exclude`）
- 支持 `@AnonymousAccess(excludeFromScan = true)` 跳过扫描
- 用于权限管理中的 API 选择器

### 权限系统
- **权限类型**：支持页面访问权限（PAGE）和操作权限（ACTION）
- **页面权限**：通过 `router` 字段配置前端路由
- **操作权限**：通过 `apiurl` 和 `methodtype` 字段配置后端 API
- **权限层级**：支持权限的层级结构（通过 `parentId` 实现）
- **动态检查**：`PermissionInterceptor` 根据请求方法和 URL 动态查询权限
- **事件驱动**：权限更新时通过 Spring Events 异步同步所有相关用户的 Token
- **细粒度控制**：支持 `admin:xxx:read`、`admin:xxx:create`、`admin:xxx:update`、`admin:xxx:delete` 等细粒度权限

### Elasticsearch 搜索
- **全文搜索**：搜索帖子和资源（包括资源文件名）
- **自动索引**：使用 `@IndexToElasticsearch` 注解和 AOP 切面异步索引
- **健康检查**：`ElasticsearchHealthChecker` 自动检测连接状态
- **自动重连**：连接失败时静默重试，不影响主程序
- **优雅降级**：服务不可用时返回 503 错误，前端显示友好提示
- **高亮显示**：搜索结果关键词高亮（支持夜间模式）

### Token 管理
- **UUID 基础**：使用 UUID 作为 Token 标识
- **Redis 存储**：Token 和用户信息存储在 Redis 中
- **动态更新**：权限更新时自动更新所有相关 Token
- **多点登录**：可配置是否允许多个客户端同时登录
- **事件同步**：使用 Spring Events 机制异步同步权限

### 文件上传
- 支持本地存储和阿里云 OSS 存储
- 配置文件路径在 `application.yml` 中
- 支持图片、文档等多种文件类型
- 自动清理密码信息（`User.clearPassword()` 方法）

## 配置说明

### application.yml 配置项

#### API 扫描配置
```yaml
api-scanner:
  load-on-startup: true  # 是否在启动时扫描 API
  exclude:
    controllers: []  # 排除的 Controller 名称（支持通配符）
    methods: []     # 排除的方法名称（支持通配符）
```

#### Token 配置
```yaml
jwt:
  secret: your-secret-key  # Token 密钥（生产环境必须修改）
  expiration: 86400000  # Token 过期时间（毫秒）
  allow-multiple-login: false  # 是否允许多点登录
```

#### Elasticsearch 配置
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    scheme: http
    connection-timeout: 5s
    socket-timeout: 60s
```

#### CORS 配置
```yaml
cors:
  allowed-origins:
    - http://localhost:5173
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
  allowed-headers:
    - "*"
  allow-credentials: true
  max-age: 3600
```

## 注意事项

1. **生产环境配置**：
   - 必须修改 JWT Secret 为更安全的密钥
   - 修改数据库和 Redis 连接信息
   - 配置正确的文件上传路径或 OSS 配置
   - 调整 CORS 配置为生产环境的前端地址

2. **Elasticsearch**：
   - Elasticsearch 为可选服务，未启动时搜索功能不可用但不会影响其他功能
   - 连接失败时会自动尝试重连，无需手动干预
   - 搜索服务不可用时会返回 503 错误，前端显示友好提示

3. **CORS 配置**：
   - 开发环境已配置允许前端跨域访问
   - 生产环境需要根据实际情况调整 CORS 配置

4. **日志配置**：
   - 开发环境默认输出 DEBUG 级别日志
   - 生产环境建议调整为 INFO 或 WARN 级别

5. **数据库**：
   - 确保 MySQL 字符集为 `utf8mb4`
   - 确保时区配置正确（Asia/Shanghai）
   - 执行 `src/main/resources/db/all_db.sql` 初始化数据库

6. **Redis**：
   - 确保 Redis 服务正常运行
   - 如果 Redis 有密码，需要在配置文件中填写
   - Redis 用于存储 Token 和 API 缓存，必须正常运行

7. **权限系统**：
   - 首次启动会自动扫描 API 接口（如果配置了 `load-on-startup: true`）
   - 权限更新时会自动同步所有相关用户的 Token
   - 建议在权限管理页面手动配置权限，而不是依赖自动扫描

## License

MIT
