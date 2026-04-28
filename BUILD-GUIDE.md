# PlayTime Tracker 构建指南

## 前置要求

- Java 21 或更高版本
- Gradle 8.x（可选，可以使用 Gradle Wrapper）

## 快速开始

### 方法 1: 使用 Gradle Wrapper（推荐）

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### 方法 2: 使用系统 Gradle

```bash
gradle build
```

## 构建输出

编译成功后，jar 文件位于：
```
build/libs/playtimetracker-1.0.0.jar
```

## 安装到服务器

1. 将 `playtimetracker-1.0.0.jar` 复制到服务器的 `mods` 文件夹
2. 重启 Minecraft 服务器
3. 模组会自动创建数据文件和启动 HTTP API

## 开发模式运行

```bash
# 运行客户端测试
gradlew runClient

# 运行服务器测试
gradlew runServer
```

## 常见问题

### Q: 提示 "Java version mismatch"
A: 确保使用 Java 21。检查方法：
```bash
java -version
```

### Q: Gradle 下载依赖很慢
A: 可以配置国内镜像源，在 `build.gradle` 中添加：
```groovy
repositories {
    maven { url 'https://maven.aliyun.com/repository/public' }
}
```

### Q: 编译时出现 "Cannot find symbol"
A: 运行以下命令清理并重新构建：
```bash
gradlew clean build
```

## 修改配置

如果需要修改 HTTP API 端口或其他参数，编辑：
- `HttpApiServer.java` - 修改 `DEFAULT_PORT`
- `ActiveTimeTracker.java` - 修改 AFK 检测阈值

## 许可证

MIT License - 自由使用和修改
