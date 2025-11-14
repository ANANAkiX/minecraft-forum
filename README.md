# Minecraft 论坛后端项目

基于 Spring Boot 3 的后端项目

## 技术栈

- Spring Boot 3
- MyBatis Plus
- MySQL 8
- Redis 7
- Spring Security + JWT
- Lomboka
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

2. 执行 `src/main/resources/db/schema.sql` 创建表结构

3. 修改 `application.yml` 中的数据库连接配置

## 运行

1. 启动 MySQL 和 Redis 服务

2. 修改 `application.yml` 配置文件

3. 运行项目：
```bash
mvn spring-boot:run
```

或者使用 IDE 直接运行 `MinecraftForumApplication.java`

## API 文档

启动项目后访问：http://localhost:8080/doc.html

## 项目结构

```
src/main/java/com/minecraftforum/
├── common/          # 通用类
├── config/          # 配置类
├── controller/      # 控制器
├── dto/             # 数据传输对象
├── entity/          # 实体类
├── mapper/          # Mapper 接口
├── security/        # 安全配置
├── service/         # 服务层
├── util/            # 工具类
└── MinecraftForumApplication.java
```

















