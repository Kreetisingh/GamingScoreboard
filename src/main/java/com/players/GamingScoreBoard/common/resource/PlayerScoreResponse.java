package com.players.GamingScoreBoard.common.resource;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerScoreResponse {

    private String playerName;
    private Integer score;

}
