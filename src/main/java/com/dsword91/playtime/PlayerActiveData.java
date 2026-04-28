package com.dsword91.playtime;

public class PlayerActiveData {
    private String playerName = "";
    private int totalActiveMinutes = 0;
    private long lastActivityTick = 0;
    private boolean wasActive = false;

    private static final int AFK_THRESHOLD_TICKS = 100;
    private static final int TICKS_PER_MINUTE = 1200;

    public PlayerActiveData() {}

    public void recordActivity(String name, long currentTick) {
        this.playerName = name;
        this.lastActivityTick = currentTick;
        this.wasActive = true;
    }

    public boolean updateActiveTime(long currentTick) {
        if (currentTick - lastActivityTick > AFK_THRESHOLD_TICKS) {
            wasActive = false;
            return false;
        }
        if (wasActive) {
            totalActiveMinutes++;
        }
        return true;
    }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getTotalActiveMinutes() { return totalActiveMinutes / TICKS_PER_MINUTE; }
    public long getLastActivityTick() { return lastActivityTick; }
    public boolean isWasActive() { return wasActive; }
    public void setWasActive(boolean wasActive) { this.wasActive = wasActive; }
}
