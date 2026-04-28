# PlayTime Tracker - 项目总览

## 项目结构

```
mc-playtime-tracker/
├── build.gradle                          # Gradle 构建配置
├── settings.gradle                       # Gradle 设置
├── gradle.properties                     # 项目属性（版本、依赖等）
├── init-gradle.bat                       # Gradle Wrapper 初始化脚本（Windows）
├── README.md                             # 使用说明
├── BUILD-GUIDE.md                        # 构建指南
├── playtime_tracker_config.example.json  # 配置文件示例
│
└── src/main/
    ├── java/com/bk/playtimetracker/
    │   ├── PlayTimeTrackerMod.java       # 模组主类（入口点）
    │   ├── DataManager.java              # 数据管理器（JSON 存储）
    │   ├── PlayerActiveData.java         # 玩家活跃数据模型
    │   ├── ActiveTimeTracker.java        # 核心追踪器（事件监听）
    │   ├── PlayTimeCommand.java          # 游戏内命令
    │   ├── HttpApiServer.java            # HTTP API 服务器
    │   └── PluginConfig.java             # 配置管理
    │
    └── resources/
        └── META-INF/
            ├── neoforge.mods.toml        # NeoForge 模组元数据
            └── accesstransformer.cfg     # 访问转换器配置
```

## 核心功能模块

### 1. ActiveTimeTracker（活跃时间追踪器）

**职责**: 监听玩家活动事件，判断是否处于活跃状态

**检测的活动类型**:
- ✅ 移动（位置变化 > 0.1 方块）
- ✅ 方块交互（放置/破坏）
- ✅ 物品使用（右键点击）
- ✅ 战斗（攻击实体/受到伤害）
- ✅ 聊天消息发送
- ✅ 维度切换

**AFK 判定逻辑**:
```
如果玩家在 5 秒（100 ticks）内没有任何活动 → 标记为 AFK
AFK 期间的时间不计入活跃时间
```

### 2. DataManager（数据管理器）

**职责**: 管理玩家数据的持久化存储

**存储格式**: JSON 文件 (`playtime_data.json`)

**数据结构**:
```json
{
  "player-uuid-1": {
    "playerName": "Player1",
    "totalActiveMinutes": 7230,
    "lastActivityTick": 123456,
    "wasActive": true
  }
}
```

**功能**:
- 自动加载/保存数据
- 定期备份（每 5 分钟）
- 提供排行榜查询接口

### 3. HttpApiServer（HTTP API 服务器）

**职责**: 提供 RESTful API 供外部系统查询

**端点**:
- `GET /playtime?top=10` - 获取活跃时间排行榜
- `GET /health` - 健康检查

**认证**: 支持 Bearer Token 认证（可选）

**响应示例**:
```json
{
  "total_players": 50,
  "leaderboard": [
    {
      "uuid": "...",
      "name": "Player1",
      "play_minutes": 7230
    }
  ]
}
```

### 4. PlayTimeCommand（游戏内命令）

**命令**:
```bash
/playtime                    # 查看自己的活跃时间
/playtime <玩家名>           # 查看指定玩家
/playtime leaderboard [N]    # 查看前 N 名排行榜
```

### 5. PluginConfig（配置管理）

**配置项**:
| 参数 | 默认值 | 说明 |
|------|--------|------|
| apiPort | 25002 | HTTP API 端口 |
| afkThresholdTicks | 100 | AFK 判定阈值（ticks） |
| movementThreshold | 0.1 | 移动检测阈值（方块） |
| saveInterval | 6000 | 数据保存间隔（ticks） |
| apiKey | "" | API 认证密钥（留空禁用） |

## 工作流程

```
[玩家活动]
    ↓
[事件监听器 ActiveTimeTracker]
    ├─ PlayerTickEvent (移动检测)
    ├─ PlayerInteractEvent (交互)
    ├─ AttackEntityEvent (战斗)
    ├─ ServerChatEvent (聊天)
    └─ ...
    ↓
[更新 playerLastActivity Map]
    ↓
[ServerTickEvent 主循环]
    ├─ 检查每个玩家的最后活动时间
    ├─ 如果在 AFK 阈值内 → 标记为活跃
    └─ 累加活跃时间（按 tick）
    ↓
[DataManager 存储]
    ├─ 内存: ConcurrentHashMap
    └─ 磁盘: playtime_data.json (定期保存)
    ↓
[HTTP API / 游戏内命令]
    └─ 查询排行榜数据
```

## 与现有系统集成

### NapCat 插件配置

修改 `hf/napcat-plugin-mcquery` 的配置：

```json
{
  "playtimeApiUrl": "http://mc.myluck.top:25002",
  "playtimeApiKey": "",  // 如果启用了 API 认证
  "playtimeCommand": "在线排行"
}
```

### FastAPI 代理（可选）

如果需要通过 FastAPI 转发请求，在 `hf/app.py` 中添加代理端点。

## 性能考虑

- **线程安全**: 使用 `ConcurrentHashMap` 保证并发安全
- **批量保存**: 每 5 分钟保存一次，减少 IO 操作
- **轻量级 HTTP**: 使用原生 Java Socket，无额外依赖
- **低开销**: 事件驱动，仅在活动时更新状态

## 扩展建议

1. **数据库支持**: 可替换 JSON 为 SQLite/MySQL
2. **Web 管理界面**: 添加可视化后台
3. **统计数据**: 增加日均活跃时间、最活跃时段等
4. **成就系统**: 基于活跃时间解锁成就
5. **多语言支持**: i18n 国际化

## 故障排查

### 问题: API 无法访问
- 检查端口是否被占用
- 检查防火墙设置
- 查看服务器日志确认 API 已启动

### 问题: 数据不更新
- 检查 `playtime_data.json` 文件权限
- 查看日志是否有保存错误
- 确认玩家确实在进行活动

### 问题: AFK 检测不准确
- 调整 `afkThresholdTicks` 参数
- 调整 `movementThreshold` 参数
- 检查是否有其他模组干扰

## 开发计划

- [x] 基础活跃时间追踪
- [x] HTTP API 支持
- [x] 游戏内命令
- [x] 配置文件支持
- [x] API 密钥认证
- [ ] Web 管理界面
- [ ] 数据库后端
- [ ] 更多统计维度

## 许可证

MIT License - 自由使用和修改
