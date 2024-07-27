package com.players.GamingScoreBoard.controller;

import com.players.GamingScoreBoard.common.resource.PlayerScoreResource;
import com.players.GamingScoreBoard.common.resource.PlayerScoreResponse;
import com.players.GamingScoreBoard.service.PlayerScoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/player-scores")
public class PlayerScoreController {

    @Autowired
    private PlayerScoreService scoreService;

    /**
     * This endpoint is used to mimic the behaviour of data insertions using flat file
     */
    @PostMapping("/read")
    public void readScoresFromFile() {
        scoreService.readScoresFromFile();
    }

    @PostMapping("/submit")
    public void submitScore(@Valid @RequestBody PlayerScoreResource resource) {
        scoreService.submitScore(resource.getPlayerName(), resource.getScore(), resource.getMatchName(),
                resource.getMatchDate(), resource.getPlayerEmail());
    }

    @GetMapping("/top")
    public List<PlayerScoreResponse> getTopScores(@RequestParam(defaultValue = "5") int limit) {
        return scoreService.getTopScores(limit);
    }
}
