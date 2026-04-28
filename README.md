# PlayTime Tracker - NeoForge 1.21.1 玩家活跃时间追踪模组

## 功能特性

✅ **智能 AFK 检测** - 只记录玩家的活跃时间，排除挂机时间

✅ **多维度活动监测**：
- 移动（位置变化）
- 方块交互（放置/破坏）
- 物品使用
- 战斗（攻击/受击）
- 聊天消息发送

✅ **HTTP API 支持** - 提供 RESTful API 供外部系统查询

✅ **游戏内命令** - 查看个人时间和排行榜

## 安装方法

1. 确保服务器已安装 NeoForge 1.21.1
2. 将编译好的 `.jar` 文件放入 `mods` 文件夹
3. 启动服务器

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

API 默认运行在端口 `25002`

```bash
# 获取活跃时间排行榜
GET http://localhost:25002/playtime?top=10

# 健康检查
GET http://localhost:25002/health
```

#### API 响应示例

```json
{
  "total_players": 50,
  "leaderboard": [
    {
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Player1",
      "play_minutes": 7230
    },
    {
      "uuid": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
      "name": "Player2",
      "play_minutes": 5895
    }
  ]
}
```

## 配置说明

### AFK 检测参数

在 `ActiveTimeTracker.java` 中可以调整以下参数：

- `AFK_THRESHOLD`: 无活动后判定为 AFK 的时间（默认 100 ticks = 5秒）
- `MOVEMENT_THRESHOLD`: 判定为有效移动的距离阈值（默认 0.1 方块）

### 数据存储

玩家数据保存在服务器根目录的 `playtime_data.json` 文件中。

## 构建方法

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

编译后的 jar 文件位于 `build/libs/` 目录。

## 与 QQ 机器人集成

修改 NapCat 插件配置，将 `playtimeApiUrl` 设置为：

```
http://mc.myluck.top:25002
```

然后发送 `在线排行` 即可查询。

## 技术细节

### 活跃判定逻辑

1. **活动事件监听**：监听玩家的移动、交互、战斗等事件
2. **时间窗口检测**：如果在 5 秒内有任意活动，视为活跃状态
3. **按 Tick 累加**：每 tick（1/20秒）检查一次，累加活跃时间
4. **转换为分钟**：最终显示时转换为分钟数

### 性能优化

- 使用 `ConcurrentHashMap` 保证线程安全
- 定期保存（每5分钟），避免频繁IO
- 轻量级 HTTP 服务器，不影响主线程

## 许可证

MIT License

## 作者

BK
