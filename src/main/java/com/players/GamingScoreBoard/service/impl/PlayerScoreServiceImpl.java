package com.players.GamingScoreBoard.service.impl;

import com.players.GamingScoreBoard.common.mapper.PlayerScoreMapper;
import com.players.GamingScoreBoard.common.model.Match;
import com.players.GamingScoreBoard.common.model.Player;
import com.players.GamingScoreBoard.common.model.PlayerScore;
import com.players.GamingScoreBoard.common.resource.PlayerScoreResponse;
import com.players.GamingScoreBoard.dao.MatchRepository;
import com.players.GamingScoreBoard.dao.PlayerRepository;
import com.players.GamingScoreBoard.dao.PlayerScoreRepository;
import com.players.GamingScoreBoard.service.PlayerScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PlayerScoreServiceImpl implements PlayerScoreService {

    private static final String SCORE_FILE_PATH = "classpath:scores.txt";
    private static final int BATCH_SIZE = 10;

    PlayerScoreMapper playerScoreMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlayerScoreRepository playerScoreRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Transactional
    public void readScoresFromFile() {
        Resource resource = resourceLoader.getResource(SCORE_FILE_PATH);
        List<PlayerScore> playerScores = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                String playerName = parts[0];
                int score = Integer.parseInt(parts[1]);
                String matchName = parts[2];
                Timestamp matchDate = Timestamp.valueOf(parts[3]);
                String playerEmail = parts[4];

                try {
                    // Find or create player, assume the email should be the same
                    Player player = playerRepository.findByPlayerNameAndEmail(playerName, playerEmail)
                            .orElseGet(() -> playerRepository.save(new Player(playerName, playerEmail)));

                    // Find or create match
                    Match match = matchRepository.findByMatchNameAndMatchDate(matchName, matchDate)
                            .orElseGet(() -> matchRepository.save(new Match(matchName, matchDate)));

                    PlayerScore playerScore = new PlayerScore();
                    playerScore.setPlayer(player);
                    playerScore.setScore(score);
                    playerScore.setInsertTimestamp(new Timestamp(System.currentTimeMillis()));
                    playerScore.setMatch(match);
                    playerScores.add(playerScore);

                    if (playerScores.size() == BATCH_SIZE) {
                        batchInsertScores(playerScores);
                        playerScores.clear();
                    }
                } catch (DataIntegrityViolationException e) {
                    log.error("Error processing line: {}. Exception: {}", line, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
        }
        if (!playerScores.isEmpty()) {
            batchInsertScores(playerScores);
        }
    }

    private void batchInsertScores(List<PlayerScore> playerScores) {
        String sql = "INSERT INTO player_scores (player_id, score, insert_timestamp, match_id) VALUES (?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();

        for (PlayerScore playerScore : playerScores) {
            batchArgs.add(new Object[]{
                    playerScore.getPlayer().getId(),
                    playerScore.getScore(),
                    playerScore.getInsertTimestamp(),
                    playerScore.getMatch().getId()
            });
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void submitScore(String playerName, int score, String matchName, Timestamp matchDate, String playerEmail) {
        Player player = playerRepository.findByPlayerNameAndEmail(playerName, playerEmail)
                .orElseGet(() -> playerRepository.save(new Player(playerName, playerEmail)));

        Match match = matchRepository.findByMatchNameAndMatchDate(matchName, matchDate)
                .orElseGet(() -> matchRepository.save(new Match(matchName, matchDate)));

        PlayerScore playerScore = new PlayerScore();
        playerScore.setPlayer(player);
        playerScore.setScore(score);
        playerScore.setInsertTimestamp(new Timestamp(System.currentTimeMillis()));
        playerScore.setMatch(match);

        batchInsertScores(List.of(playerScore));
    }

    @Override
    public List<PlayerScoreResponse> getTopScores(int limit) {
        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(0, limit);
        List<PlayerScore> topScores = playerScoreRepository.findTopScoresSortedByMatchDate(pageable).getContent();

        log.info("Fetched {} top scores in {}ms",
                Optional.ofNullable(topScores).map(List::size)
                        .orElse(0), System.currentTimeMillis() - startTime);

        return playerScoreMapper.toPlayerScoreResponseList(topScores);
    }
}