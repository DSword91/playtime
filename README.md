# PlayTime - NeoForge Player Active Time Tracker

A lightweight NeoForge 1.21.1 mod that tracks players' **active time** (excluding AFK/idle time).

## Features

- **Smart AFK Detection** - Only records time when players are actually active
- **Multi-dimensional Activity Tracking**:
  - Movement (position changes)
  - Block interaction (placing/breaking)
  - Item usage
  - Combat (attacking/taking damage)
  - Chat messages
- **HTTP API** - RESTful API on port 25002 for external queries
- **In-game Commands** - `/playtime` and `/playtime leaderboard`
- **Data Persistence** - JSON file storage with auto-save every 5 minutes

## Installation

1. Install **NeoForge 1.21.1** on your server
2. Download `playtime-1.0.0.jar` from [Releases](https://github.com/DSword91/playtime/releases)
3. Place the JAR file in your server's `mods` folder
4. Start the server

## Usage

### In-game Commands

```bash
/playtime                    # View your own active time
/playtime <player>           # View a specific player's time
/playtime leaderboard        # View top 10 leaderboard
/playtime leaderboard 20     # View top 20
```

### HTTP API

```bash
# Get leaderboard
GET http://localhost:25002/playtime?top=10

# Health check
GET http://localhost:25002/health
```

**Response Example:**
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

## Configuration

Config file is generated at `config/playtime_config.json`:

```json
{
  "apiPort": 25002,
  "afkThresholdTicks": 100,
  "movementThreshold": 0.1,
  "saveInterval": 6000,
  "apiKey": ""
}
```

## Build from Source

```bash
git clone https://github.com/DSword91/playtime.git
cd playtime
./gradlew build
```

The JAR will be in `build/libs/playtime-1.0.0.jar`.

## Performance

- **CPU Usage**: < 0.5% (negligible)
- **Memory Usage**: ~10-50 MB
- **TPS Impact**: None

The mod uses an event-driven architecture with minimal computation overhead.

## License

MIT License

## Author

**DSword91**
