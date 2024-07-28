package com.players.GamingScoreBoard.controller;

import com.players.GamingScoreBoard.common.resource.PlayerScoreResponse;
import com.players.GamingScoreBoard.service.PlayerScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/player-scores")
public class PlayerScoreController {

    @Autowired
    private PlayerScoreService scoreService;

    /**
     * This endpoint is used to mimic the behaviour of data insertions using flat file.
     *
     * @param file MultipartFile containing the CSV data.
     * @return ResponseEntity with status and message.
     */
    @PostMapping("/read")
    public ResponseEntity<String> readScoresFromFile(@RequestParam("file") MultipartFile file) {
        try {
            scoreService.readScoresFromFile(file);
            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        }
    }

    /**
     * This endpoint is used to get the top scores with the player names.
     *
     * @param limit Maximum number of top scores to fetch, default value 5
     * @return ResponseEntity with list of PlayerScoreResponse and status.
     */
    @GetMapping("/top")
    public ResponseEntity<List<PlayerScoreResponse>> getTopScores(@RequestParam(defaultValue = "5") int limit) {
        List<PlayerScoreResponse> topScores = scoreService.getTopScores(limit);
        if (topScores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(topScores);
        } else {
            return ResponseEntity.ok(topScores);
        }
    }
}

