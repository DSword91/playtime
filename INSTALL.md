# PlayTime Tracker - 构建与使用指南

## 重要说明

由于 NeoForge 1.21.1 的依赖较为复杂，推荐使用以下方式之一来构建模组。

---

## 方法一：使用 IntelliJ IDEA（推荐）

### 步骤

1. **安装 IntelliJ IDEA**
   - 下载：https://www.jetbrains.com/idea/download/
   - 推荐使用 Ultimate 版本（社区版也可用）

2. **打开项目**
   ```
   File → Open → 选择 mc-playtime-tracker 文件夹
   ```

3. **等待 Gradle 同步**
   - IDEA 会自动下载所有依赖
   - 首次同步可能需要 5-10 分钟

4. **构建 JAR**
   ```
   右侧 Gradle 面板 → Tasks → build → jar
   ```

5. **找到输出文件**
   ```
   build/libs/playtimetracker-1.0.0.jar
   ```

---

## 方法二：使用命令行 Gradle

### 前置要求

- Java 21 或更高版本
- Gradle 8.8+

### 构建命令

```bash
cd mc-playtime-tracker

# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### 如果遇到依赖下载问题

1. 检查网络连接
2. 尝试使用代理
3. 清理缓存后重试：
   ```bash
   gradle clean build --refresh-dependencies
   ```

---

## 方法三：手动编译（高级）

### 步骤

1. **下载 NeoForge 开发环境 JAR**
   ```
   https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.65/neoforge-21.1.65.jar
   ```

2. **设置类路径并编译**
   ```bash
   # 创建输出目录
   mkdir -p build/classes

   # 编译所有 Java 文件
   javac -encoding UTF-8 -source 21 -target 21 \
     -cp "neoforge-21.1.65.jar" \
     -d build/classes \
     $(find src/main/java -name "*.java")

   # 复制资源文件
   cp -r src/main/resources/* build/classes/

   # 打包 JAR
   cd build/classes
   jar cf ../playtimetracker-1.0.0.jar .
   ```

---

## 安装到服务器

### 1. 准备服务器

确保你的 Minecraft 服务器已安装 **NeoForge 1.21.1**

下载地址：
```
https://neoforged.net/
```

### 2. 安装模组

将编译好的 `playtimetracker-1.0.0.jar` 复制到服务器的 `mods` 文件夹：

```
your-server/
├── mods/
│   └── playtimetracker-1.0.0.jar  ← 放在这里
├── world/
├── server.properties
└── ...
```

### 3. 启动服务器

```bash
# Windows
run.bat

# Linux
./run.sh
```

### 4. 验证安装

查看服务器日志，应该看到类似输出：
```
[PlayTime Tracker] PlayTime Tracker 模组已初始化 - 开始追踪玩家活跃时间
[PlayTime Tracker] HTTP API 服务器已启动在端口 25002
```

---

## 配置模组

### 配置文件位置

服务器根目录会生成 `playtime_tracker_config.json`

### 配置项说明

```json
{
  "apiPort": 25002,           // HTTP API 端口
  "afkThresholdTicks": 100,   // AFK 判定阈值（100 ticks = 5秒）
  "movementThreshold": 0.1,   // 移动检测阈值（方块距离）
  "saveInterval": 6000,       // 数据保存间隔（6000 ticks = 5分钟）
  "apiKey": ""                // API 认证密钥（留空则不启用）
}
```

### 修改配置后

重启服务器使配置生效。

---

## 使用方法

### 游戏内命令

```bash
# 查看自己的活跃时间
/playtime

# 查看指定玩家的活跃时间
/playtime <玩家名>

# 查看排行榜（默认前10名）
/playtime leaderboard

# 查看前N名
/playtime leaderboard 20
```

### HTTP API

#### 获取排行榜

```bash
GET http://localhost:25002/playtime?top=10
```

**响应示例：**
```json
{
  "total_players": 50,
  "leaderboard": [
    {
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Player1",
      "play_minutes": 7230
    }
  ]
}
```

#### 健康检查

```bash
GET http://localhost:25002/health
```

**响应：**
```json
{
  "status": "ok"
}
```

---

## 与 QQ 机器人集成

### 修改 NapCat 插件配置

编辑 `hf/napcat-plugin-mcquery` 的配置：

```json
{
  "playtimeApiUrl": "http://mc.myluck.top:25002",
  "playtimeApiKey": "",  // 如果启用了 API 认证，填写密钥
  "playtimeCommand": "在线排行"
}
```

### 测试

在 QQ 群中发送：
```
在线排行
```

应该返回类似：
```
📊 活跃时间排行 (共50人)
━━━━━━━━━━━━━━
🥇 Player1 — 120小时30分
🥈 Player2 — 98小时15分
🥉 Player3 — 76小时45分
...
```

---

## 故障排查

### 问题 1：模组未加载

**症状：** 服务器日志中没有 PlayTime Tracker 相关信息

**解决方法：**
1. 确认服务器使用的是 NeoForge（不是 Forge 或 Fabric）
2. 确认 NeoForge 版本是 1.21.1
3. 检查 `mods` 文件夹中是否有 JAR 文件
4. 查看 `logs/latest.log` 中的错误信息

### 问题 2：API 无法访问

**症状：** 访问 `http://localhost:25002/playtime` 超时

**解决方法：**
1. 检查防火墙是否阻止了端口 25002
2. 确认服务器已完全启动
3. 查看配置文件中的 `apiPort` 设置
4. 尝试使用 `netstat -an | grep 25002` 检查端口监听状态

### 问题 3：数据不更新

**症状：** 玩家在线时间一直为 0

**解决方法：**
1. 确认玩家确实在进行活动（移动、交互等）
2. 检查 `playtime_data.json` 文件是否存在且有内容
3. 查看服务器日志是否有错误
4. 尝试调整 `afkThresholdTicks` 参数

### 问题 4：编译失败

**症状：** Gradle 构建时出现依赖错误

**解决方法：**
1. 使用 IntelliJ IDEA 打开项目（自动处理依赖）
2. 清理 Gradle 缓存：`gradle clean --refresh-dependencies`
3. 检查网络连接，可能需要使用代理
4. 手动下载 NeoForge JAR 并放入本地 Maven 仓库

---

## 技术支持

如遇到问题，请提供以下信息：
1. 服务器版本（NeoForge x.x.x）
2. 完整的错误日志
3. 配置文件内容
4. 复现步骤

---

## 许可证

MIT License - 自由使用和修改
