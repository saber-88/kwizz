package com.kwizz.dto;

public class LeaderboardEntry {
    private String nickname;
    private int score;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String nickname, int score) {
        this.nickname = nickname;
        this.score = score;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}