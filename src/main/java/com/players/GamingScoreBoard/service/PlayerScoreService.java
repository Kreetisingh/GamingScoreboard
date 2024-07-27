package com.players.GamingScoreBoard.service;

import com.players.GamingScoreBoard.common.resource.PlayerScoreResponse;

import java.sql.Timestamp;
import java.util.List;

public interface PlayerScoreService {
    void readScoresFromFile();

    void submitScore(String playerName, int score, String matchName, Timestamp matchDate, String playerEmail);

    List<PlayerScoreResponse> getTopScores(int limit);
}
