package com.players.GamingScoreBoard.common.resource;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerScoreResource {
    @NotNull
    private String playerName;
    @NotNull
    private Integer score;
    @NotNull
    private String matchName;
    @NotNull
    private Timestamp matchDate;
    @NotNull
    private String playerEmail;
}
