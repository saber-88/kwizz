package com.kwizz.dto;

import java.util.List;

public class LeaderboardMessage {
    private List<LeaderboardEntry> entries;

    public LeaderboardMessage() {}

    public LeaderboardMessage(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }

    public List<LeaderboardEntry> getEntries() { return entries; }
    public void setEntries(List<LeaderboardEntry> entries) { this.entries = entries; }
}