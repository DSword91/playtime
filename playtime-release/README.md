# PlayTime - Minecraft 玩家活跃时间追踪模组

**版本**: Forge 1.12.2 (CatRoom/Cleanroom)

一个轻量级的 Forge 1.12.2 模组，用于追踪玩家的**活跃游戏时间**（排除挂机/空闲时间）。

> **注意**: 此版本适用于 Minecraft 1.12.2 + CatRoom/Cleanroom/Forge
> 
> 如需 NeoForge 1.21.1 版本，请查看 main 分支

## 功能特性

- **智能 AFK 检测** - 只记录玩家实际活跃的时间
- **多维度活动追踪**：
  - 移动（位置变化）
  - 方块交互（放置/破坏）
  - 物品使用
  - 战斗（攻击/受到伤害）
  - 聊天消息
- **HTTP API** - 运行在 25002 端口的 RESTful API，支持外部查询
- **游戏内命令** - `/playtime` 和 `/playtime leaderboard`
- **数据持久化** - JSON 文件存储，每 5 分钟自动保存

## 安装方法

1. 在服务器上安装 **CatRoom/Cleanroom/Forge 1.12.2**
2. 将 `playtime-1.0.0.jar` 放入服务器的 `mods` 文件夹
3. 启动服务器
4. 配置文件将自动生成在 `config/playtime_config.json`

## 使用方法

### 游戏内命令

```bash
/playtime                    # 查看自己的活跃时间
/playtime <玩家名>           # 查看指定玩家的活跃时间
/playtime leaderboard        # 查看前 10 名排行榜
/playtime leaderboard 20     # 查看前 20 名
```

### HTTP API

```bash
# 获取排行榜
GET http://localhost:25002/playtime?top=10

# 健康检查
GET http://localhost:25002/health
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

## 配置说明

配置文件位于 `config/playtime_config.json`：

```json
{
  "apiPort": 25002,
  "afkThresholdTicks": 100,
  "movementThreshold": 0.1,
  "saveInterval": 6000,
  "apiKey": ""
}
```

**配置项说明：**
- `apiPort`: HTTP API 端口（默认 25002）
- `afkThresholdTicks`: AFK 判定阈值（tick 数，默认 100）
- `movementThreshold`: 移动判定阈值（默认 0.1）
- `saveInterval`: 数据保存间隔（tick 数，默认 6000 = 5 分钟）
- `apiKey`: API 密钥（可选，用于保护 API）

## 性能表现

- **CPU 占用**: < 0.5%（几乎无影响）
- **内存占用**: ~10-50 MB
- **TPS 影响**: 无

模组采用事件驱动架构，计算开销极小。

## 从源码构建

### 环境要求
- **JDK 8** (推荐 Zulu 8 或 AdoptOpenJDK 8)
- Gradle 8.x

### 编译步骤

```bash
git clone https://github.com/DSword91/playtime.git
cd playtime
git checkout feature/1.12.2-catroom-migration

# Windows (使用 Java 8)
& 'C:\Program Files\Java\zulu8.80.0.17-ca-jdk8.0.422-win_x64\bin\java.exe' -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build --no-daemon

# Linux/Mac (确保 JAVA_HOME 指向 Java 8)
./gradlew build
```

生成的 JAR 文件位于 `build/libs/playtime-1.0.0.jar`。

## 作者

**DSword91**

## 许可证

MIT License
