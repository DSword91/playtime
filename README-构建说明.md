# PlayTime Tracker - 重要说明

## ⚠️ 当前状态

所有源代码已经编写完成，但由于**网络连接问题**，无法自动下载 NeoForge 依赖库来完成构建。

---

## 📦 项目已完成的内容

✅ **完整的源代码**（7个Java文件）
- `PlayTimeTrackerMod.java` - 模组主类
- `ActiveTimeTracker.java` - 核心追踪器
- `DataManager.java` - 数据管理
- `PlayerActiveData.java` - 数据模型
- `HttpApiServer.java` - HTTP API 服务器
- `PlayTimeCommand.java` - 游戏内命令
- `PluginConfig.java` - 配置管理

✅ **完整的配置文件**
- `build.gradle` - Gradle 构建配置
- `neoforge.mods.toml` - NeoForge 模组元数据
- `playtime_tracker_config.example.json` - 配置示例

✅ **完整的文档**
- `快速开始.md` - 3步上手指南
- `INSTALL.md` - 详细安装说明
- `手动构建指南.md` - 手动构建步骤
- `PROJECT-OVERVIEW.md` - 技术架构
- `README.md` - 完整功能文档

---

## 🔧 如何完成构建

### 方案 A：使用 IntelliJ IDEA（推荐）⭐

1. 下载 IntelliJ IDEA：https://www.jetbrains.com/idea/download/
2. 打开项目文件夹：`c:\Users\59485\Desktop\ds\BK\mc-playtime-tracker`
3. 等待 Gradle 同步（IDEA 会自动处理依赖下载）
4. 右侧 Gradle 面板 → Tasks → build → jar
5. 输出：`build/libs/playtimetracker-1.0.0.jar`

### 方案 B：手动下载依赖后构建

1. **下载 NeoForge JAR**
   - 访问：https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.65/
   - 下载：`neoforge-21.1.65.jar`

2. **放置依赖**
   ```
   复制到：mc-playtime-tracker/lib/neoforge.jar
   ```

3. **运行构建脚本**
   ```bash
   double-click: build-complete.bat
   ```

### 方案 C：改善网络环境后构建

如果你能：
- 使用代理/VPN
- 切换到更好的网络
- 使用手机热点

然后运行：
```bash
cd c:\Users\59485\Desktop\ds\BK\mc-playtime-tracker
gradle build --no-daemon
```

---

## 📋 构建后的使用流程

### 1. 安装到服务器

```bash
# 复制 JAR 到服务器 mods 文件夹
cp build/libs/playtimetracker-1.0.0.jar /你的服务器/mods/
```

### 2. 启动服务器

确保服务器已安装 **NeoForge 1.21.1**

### 3. 验证安装

查看日志应显示：
```
[PlayTime Tracker] HTTP API 服务器已启动在端口 25002
```

### 4. 测试功能

**游戏内：**
```bash
/playtime
/playtime leaderboard
```

**HTTP API：**
```bash
curl http://localhost:25002/playtime?top=10
```

### 5. 集成 QQ 机器人

修改 `hf/napcat-plugin-mcquery` 配置：
```json
{
  "playtimeApiUrl": "http://mc.myluck.top:25002",
  "playtimeCommand": "在线排行"
}
```

---

## 🎯 核心功能预览

### AFK 检测逻辑

```
玩家活动事件 → 更新最后活动时间戳
                ↓
每 tick 检查 → 如果 5秒内有活动 → 累加活跃时间
                ↓
            如果 5秒内无活动 → 标记为 AFK（不计入时间）
```

### 检测的活动类型

- ✅ 移动（位置变化 > 0.1 方块）
- ✅ 方块交互（放置/破坏）
- ✅ 物品使用
- ✅ 战斗（攻击/受击）
- ✅ 聊天消息
- ✅ 维度切换

### API 响应示例

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

---

## 📊 项目文件清单

```
mc-playtime-tracker/
├── src/main/java/com/bk/playtimetracker/
│   ├── PlayTimeTrackerMod.java      (1.2 KB) ✅
│   ├── ActiveTimeTracker.java       (8.5 KB) ✅
│   ├── DataManager.java             (3.2 KB) ✅
│   ├── PlayerActiveData.java        (2.1 KB) ✅
│   ├── HttpApiServer.java           (6.8 KB) ✅
│   ├── PlayTimeCommand.java         (4.5 KB) ✅
│   └── PluginConfig.java            (2.8 KB) ✅
│
├── src/main/resources/META-INF/
│   ├── neoforge.mods.toml           (0.8 KB) ✅
│   └── accesstransformer.cfg        (0.2 KB) ✅
│
├── build.gradle                     (1.5 KB) ✅
├── gradle.properties                (0.5 KB) ✅
├── settings.gradle                  (0.3 KB) ✅
│
├── 快速开始.md                      ✅
├── INSTALL.md                       ✅
├── 手动构建指南.md                   ✅
├── PROJECT-OVERVIEW.md              ✅
├── README.md                        ✅
└── build-complete.bat               ✅
```

---

## 💡 下一步建议

1. **优先尝试 IntelliJ IDEA** - 最可靠的依赖管理
2. **如果网络受限** - 找一台能访问 Maven 仓库的机器构建
3. **寻求社区帮助** - 在 Minecraft 模组开发社区询问
4. **学习参考** - 即使不构建，代码也可作为学习 NeoForge 开发的参考

---

## 🆘 需要帮助？

如果遇到问题：

1. 查看详细文档：`手动构建指南.md`
2. 检查 NeoForge 官方文档：https://docs.neoforged.net/
3. 在社区提问时提供：
   - 错误日志
   - Java 版本
   - Gradle 版本
   - 网络环境描述

---

**祝你好运！🎮**

所有代码都已经准备就绪，只需要解决依赖下载问题即可完成构建。
