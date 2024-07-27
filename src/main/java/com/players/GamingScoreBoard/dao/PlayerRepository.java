package com.players.GamingScoreBoard.dao;

import com.players.GamingScoreBoard.common.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPlayerNameAndEmail(String playerName, String email);
}
