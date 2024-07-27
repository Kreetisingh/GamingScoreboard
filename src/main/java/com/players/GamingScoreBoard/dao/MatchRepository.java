package com.players.GamingScoreBoard.dao;

import com.players.GamingScoreBoard.common.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchNameAndMatchDate(String matchName, Timestamp matchDate);
}
