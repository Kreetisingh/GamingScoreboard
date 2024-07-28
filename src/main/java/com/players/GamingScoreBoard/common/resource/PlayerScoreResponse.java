package com.players.GamingScoreBoard.common.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerScoreResponse {

    private String playerName;
    private Integer score;
    
}
