package com.players.GamingScoreBoard.dao;


import com.players.GamingScoreBoard.common.model.PlayerScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, Long> {
    @Query(value = "WITH TopScores AS ( " +
            "    SELECT ps.id, ps.player_id, ps.match_id, ps.score, ps.insert_timestamp " +
            "    FROM player_scores ps " +
            "    JOIN matches m ON ps.match_id = m.id " +
            "    ORDER BY ps.score DESC " +
            ") " +
            "SELECT ts.*, m.match_date " +
            "FROM TopScores ts " +
            "JOIN matches m ON ts.match_id = m.id " +
            "ORDER BY ts.score DESC, m.match_date DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM ( " +
                    "    SELECT ps.id " +
                    "    FROM player_scores ps " +
                    "    JOIN matches m ON ps.match_id = m.id " +
                    "    ORDER BY ps.score DESC " +
                    ") AS count_table",
            nativeQuery = true)
    Page<PlayerScore> findTopScoresSortedByMatchDate(Pageable pageable);
}
