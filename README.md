# PlayTime - 玩家活跃时间追踪模组

NeoForge 1.21.1 轻量级模组，追踪玩家**实际活跃时间**（自动排除挂机时间）。

## 功能

- **智能 AFK 检测** — 只记录玩家真正活跃的时间
- **多维度活动追踪** — 移动、方块交互、物品使用、战斗、聊天
- **HTTP API** — RESTful 接口（默认端口 25002），支持外部系统查询
- **游戏内命令** — `/playtime` 查看时间，`/playtime leaderboard` 查看排行
- **自动保存** — JSON 存储，每 5 分钟自动保存

## 安装

1. 安装 **NeoForge 1.21.1**
2. 从 [Releases](https://github.com/DSword91/playtime/releases) 下载 `playtime-1.0.0.jar`
3. 放入服务器 `mods` 文件夹
4. 启动服务器

## 使用

```bash
/playtime                    # 查看自己的活跃时间
/playtime <玩家名>           # 查看指定玩家
/playtime leaderboard        # 查看前 10 名排行
/playtime leaderboard 20     # 查看前 20 名
```

### HTTP API

```bash
GET http://localhost:25002/playtime?top=10
GET http://localhost:25002/health
```

## 配置

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

## 从源码构建

```bash
git clone https://github.com/DSword91/playtime.git
cd playtime
./gradlew build
```

生成的 JAR 在 `build/libs/playtime-1.0.0.jar`。

## 性能

- **CPU**: < 0.5%
- **内存**: ~10-50 MB
- **TPS**: 无影响

## 许可证

MIT License

**作者**: DSword91
