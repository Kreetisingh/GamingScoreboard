package com.players.GamingScoreBoard.service;

import com.players.GamingScoreBoard.common.resource.PlayerScoreResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PlayerScoreService {
    void readScoresFromFile(MultipartFile file) throws IOException;

    List<PlayerScoreResponse> getTopScores(int limit);
}
