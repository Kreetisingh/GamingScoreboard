package com.players.GamingScoreBoard.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @NonNull
    @Column(name = "player_name", nullable = false)
    private String playerName;

    @NotBlank
    @NonNull
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "player")
    private Set<PlayerScore> scores;

    @ManyToMany
    @JoinTable(
            name = "player_matches",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    private Set<Match> matches = new HashSet<>();
}
