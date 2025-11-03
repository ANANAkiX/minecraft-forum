# 重建项目说明

## 问题
项目仍在使用 Spring Boot 3.2.0，虽然 pom.xml 已更新为 3.1.5，但依赖缓存未更新。

## 解决步骤

### 方法 1：使用 IntelliJ IDEA（推荐）

1. **右键点击项目根目录（minecraft-forum）**
   - 选择 `Maven` → `Reload Project`

2. **清理并重新构建**
   - File → Invalidate Caches / Restart → Invalidate and Restart
   - Build → Rebuild Project

3. **强制更新依赖**
   - 打开 Maven 工具窗口（右侧边栏）
   - 点击刷新按钮（Reload All Maven Projects）
   - 右键项目 → `Maven` → `Download Sources and Documentation`

### 方法 2：使用命令行

在项目根目录（minecraft-forum）执行：

```bash
# 清理项目
mvn clean

# 删除 Spring Boot 3.2.0 的依赖缓存
rmdir /s /q "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-starter-parent\3.2.0"

# 强制更新并重新构建
mvn clean install -U
```

### 方法 3：如果问题仍然存在

手动删除整个 Spring Boot 缓存：
```bash
# 删除 Spring Boot 的所有缓存（会重新下载，但确保版本正确）
rmdir /s /q "%USERPROFILE%\.m2\repository\org\springframework\boot"
```

然后重新构建：
```bash
mvn clean install
```

## 验证

启动项目后，查看日志应该显示：
```
:: Spring Boot ::                (v3.1.5)
```

而不是：
```
:: Spring Boot ::                (v3.2.0)
```

