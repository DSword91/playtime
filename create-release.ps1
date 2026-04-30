# PowerShell script to create GitHub Release and upload asset
$token = $env:GITHUB_TOKEN
$owner = "DSword91"
$repo = "playtime"
$tag = "v1.0.0-1.12.2"
$jarPath = "playtime-release/playtime-1.0.0-1.12.2.jar"

if (-not $token) {
    Write-Error "Please set GITHUB_TOKEN environment variable"
    exit 1
}

$headers = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
    "Content-Type" = "application/json"
}

# Create release
$body = @{
    tag_name = $tag
    name = "PlayTime v1.0.0 for Minecraft 1.12.2 (CatRoom/Cleanroom)"
    body = @"
## PlayTime Tracker - Minecraft 1.12.2

适用于 **CatRoom** 和 **Cleanroom** 平台的玩家活跃时间追踪模组。

### ✨ 核心功能

- 📊 **活跃时间追踪**：精确记录玩家在服务器的实际活跃时间
- 🚫 **AFK检测**：自动排除挂机时间（10秒无操作视为AFK）
- 🌐 **HTTP API**：提供RESTful API接口查询玩家数据
- 💬 **游戏命令**：使用 `/playtime` 查看玩家在线时长

### 🔧 安装要求

- **服务端**：CatRoom 或 Cleanroom（基于 Forge 1.12.2）
- **客户端**：无需安装任何模组
- **Java版本**：Java 8

### 📦 安装方法

1. 将 `playtime-1.0.0-1.12.2.jar` 放入服务器的 `mods` 文件夹
2. 启动服务器
3. 配置文件自动生成于 `config/playtime_tracker_config.json`

### 🎯 使用说明

#### 游戏内命令
- `/playtime` - 查看自己的活跃时间
- `/playtime <player>` - 查看指定玩家的活跃时间
- `/playtime top [limit]` - 查看活跃时间排行榜

#### HTTP API
默认端口：8080（可在配置文件中修改）

- `GET /api/players` - 获取所有玩家数据
- `GET /api/player/<uuid>` - 获取指定玩家数据
- `GET /api/top?limit=10` - 获取排行榜

### 🛠️ 本次更新

**重要修复**：
- ✅ 修复 mcmod.info 格式为数组格式以符合 Forge 1.12.2 要求
- ✅ 添加 @NetworkCheckHandler 允许客户端不安装模组即可连接
- ✅ 移除 PlayerContainerEvent 监听器避免崩溃
- ✅ 统一 AFK 检测阈值为 10 秒（200 ticks）
- ✅ 修复 onServerStopped 使用正确的 @Mod.EventHandler 注解
- ✅ 优化 Gradle 资源文件处理配置

### ⚠️ 注意事项

- 此模组为**纯服务端模组**，客户端无需安装
- 首次运行会自动生成配置文件
- 数据保存在 `world/data/playtime_data.json`

### 📝 配置文件示例

```json
{
  "api_port": 8080,
  "afk_threshold_ticks": 200,
  "save_interval_seconds": 300
}
```

---

**项目地址**: https://github.com/DSword91/playtime  
**问题反馈**: https://github.com/DSword91/playtime/issues
"@
    draft = $false
    prerelease = $false
} | ConvertTo-Json

Write-Host "Creating release..." -ForegroundColor Cyan
$response = Invoke-RestMethod -Uri "https://api.github.com/repos/$owner/$repo/releases" -Method Post -Headers $headers -Body $body

$releaseId = $response.id
Write-Host "Release created with ID: $releaseId" -ForegroundColor Green

# Upload asset
Write-Host "Uploading asset..." -ForegroundColor Cyan
$assetHeaders = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
    "Content-Type" = "application/java-archive"
}

$uploadUrl = $response.upload_url -replace "\{.*\}", ""
$fileBytes = [System.IO.File]::ReadAllBytes($jarPath)

Invoke-RestMethod -Uri "$uploadUrl?name=playtime-1.0.0-1.12.2.jar" -Method Post -Headers $assetHeaders -Body $fileBytes | Out-Null

Write-Host "Asset uploaded successfully!" -ForegroundColor Green
Write-Host "Release URL: $($response.html_url)" -ForegroundColor Yellow
