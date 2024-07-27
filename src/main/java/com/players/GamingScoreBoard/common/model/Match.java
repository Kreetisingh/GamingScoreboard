package com.players.GamingScoreBoard.common.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "match_name", nullable = false)
    private String matchName;

    @NonNull
    @Column(name = "match_date", nullable = false)
    private Timestamp matchDate;

    @OneToMany(mappedBy = "match")
    private Set<PlayerScore> scores = new HashSet<>();

    @ManyToMany(mappedBy = "matches")
    private Set<Player> players = new HashSet<>();
}
