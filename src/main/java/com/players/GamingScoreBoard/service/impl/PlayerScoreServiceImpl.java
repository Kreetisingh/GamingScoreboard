package com.players.GamingScoreBoard.service.impl;

import com.players.GamingScoreBoard.common.mapper.PlayerScoreMapper;
import com.players.GamingScoreBoard.common.model.Match;
import com.players.GamingScoreBoard.common.model.Player;
import com.players.GamingScoreBoard.common.model.PlayerScore;
import com.players.GamingScoreBoard.common.resource.PlayerScoreResponse;
import com.players.GamingScoreBoard.common.utility.ListUtils;
import com.players.GamingScoreBoard.dao.MatchRepository;
import com.players.GamingScoreBoard.dao.PlayerRepository;
import com.players.GamingScoreBoard.dao.PlayerScoreRepository;
import com.players.GamingScoreBoard.service.PlayerScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Service implementation for handling player scores.
 */
@Service
@Slf4j
public class PlayerScoreServiceImpl implements PlayerScoreService {

    private static final int BATCH_SIZE = 10;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlayerScoreRepository playerScoreRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    private PlayerScoreMapper playerScoreMapper;

    /**
     * Reads scores from a txt file and processes them in batches.
     * (This method is to replicate the player-service pushing player scores after a match)
     *
     * @param file MultipartFile containing the CSV data.
     *             Data is expected to be in format:{name, score, match-name, match-date(yyyy-MM-dd HH:mm:ss), email}
     * @throws IOException if there's an error reading the file.
     */
    /**
     * Reads scores from a txt file and processes them in batches.
     * (This method is to replicate the player-service pushing player scores after a match)
     *
     * @param file MultipartFile containing the CSV data.
     *             Data is expected to be in format: {name, score, match-name, match-date(yyyy-MM-dd HH:mm:ss), email}
     * @throws IOException if there's an error reading the file.
     */
    public void readScoresFromFile(MultipartFile file) throws IOException {
        List<PlayerScore> playerScores = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            playerScores = br.lines()
                    .map(this::processLine) // Process each line into a PlayerScore
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList());

            // Partition the list and insert in batches with a delay
            ListUtils.partition(playerScores, BATCH_SIZE).forEach(batch -> {
                batchInsertScoresWithTransaction(batch);
                try {
                    Thread.sleep(5000); // 5 seconds delay
                } catch (InterruptedException e) {
                    log.error("Interrupted while waiting between batch inserts: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            });
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Processes a single line from the file.
     *
     * @param line A line from the CSV data.
     * @return Optional containing PlayerScore if processing is successful.
     */
    private Optional<PlayerScore> processLine(String line) {
        try {
            String[] parts = line.split(", ");
            String playerName = parts[0];
            int score = Integer.parseInt(parts[1]);
            String matchName = parts[2];
            Timestamp matchDate = Timestamp.valueOf(parts[3]);
            String playerEmail = parts[4];

            // Find or create player
            Player player = playerRepository.findByPlayerNameAndEmail(playerName, playerEmail)
                    .orElseGet(() -> playerRepository.save(new Player(playerName, playerEmail)));

            // Find or create match
            Match match = matchRepository.findByMatchNameAndMatchDate(matchName, matchDate)
                    .orElseGet(() -> matchRepository.save(new Match(matchName, matchDate)));

            // Create and populate PlayerScore object
            PlayerScore playerScore = new PlayerScore();
            playerScore.setPlayer(player);
            playerScore.setScore(score);
            playerScore.setInsertTimestamp(new Timestamp(System.currentTimeMillis()));
            playerScore.setMatch(match);
            return Optional.of(playerScore);
        } catch (DataIntegrityViolationException e) {
            log.error("Error processing line: {}. Exception: {}", line, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Inserts a batch of player scores into the database within a new transaction.
     * Here we use batch level transactions, to commit the to the database after each batch
     * This allows us to visualize the data consumed from topics after each match
     *
     * @param playerScores List of PlayerScore objects to be inserted.
     */
    private void batchInsertScoresWithTransaction(List<PlayerScore> playerScores) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            batchInsertScores(playerScores);
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            transactionManager.rollback(status);
            log.error("Error while saving data through batch update: {}", e.getMessage());
        }
    }

    /**
     * Inserts a batch of player scores into the database.
     *
     * @param playerScores List of PlayerScore objects to be inserted.
     */
    private void batchInsertScores(List<PlayerScore> playerScores) {
        String sql = "INSERT INTO player_scores (player_id, score, insert_timestamp, match_id) VALUES (?, ?, ?, ?)";
        List<Object[]> batchArgs = playerScores.stream()
                .map(score -> new Object[]{
                        score.getPlayer().getId(),
                        score.getScore(),
                        score.getInsertTimestamp(),
                        score.getMatch().getId()
                })
                .collect(toList());

        try {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (DataAccessException e) {
            log.error("Error while saving data through batch update: {}", e.getMessage());
        }
    }

    /**
     * Fetches the top scores.
     *
     * @param limit Maximum number of scores to fetch.
     * @return List of PlayerScoreResponse containing the top scores.
     */
    @Override
    public List<PlayerScoreResponse> getTopScores(int limit) {
        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(0, limit);
        List<PlayerScore> topScores = playerScoreRepository.findTopScoresSortedByMatchDate(pageable).getContent();

        log.info("Fetched {} top scores in {}ms",
                Optional.ofNullable(topScores).map(List::size).orElse(0), System.currentTimeMillis() - startTime);

        return playerScoreMapper.toPlayerScoreResponseList(topScores);
    }
}