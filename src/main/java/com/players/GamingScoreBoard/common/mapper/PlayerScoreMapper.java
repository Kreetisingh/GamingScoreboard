package com.players.GamingScoreBoard.common.mapper;

import com.players.GamingScoreBoard.common.model.PlayerScore;
import com.players.GamingScoreBoard.common.resource.PlayerScoreResponse;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class PlayerScoreMapper {

    /**
     * This mapper method would be used to convert PlayerScore object list to PlayerScoreResponse list
     * for returning as response
     *
     * @param playerScoreList
     * @return
     */
    public List<PlayerScoreResponse> toPlayerScoreResponseList(List<PlayerScore> playerScoreList) {
        return Optional.ofNullable(playerScoreList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(ps -> ps.getPlayer() != null)
                .filter(ps -> ps.getPlayer().getPlayerName() != null)
                .map(ps -> new PlayerScoreResponse(ps.getPlayer().getPlayerName(), ps.getScore()))
                .collect(toList());
    }
    
}
