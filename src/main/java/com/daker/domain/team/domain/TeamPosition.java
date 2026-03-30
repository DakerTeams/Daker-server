package com.daker.domain.team.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_positions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 100)
    private String positionName;

    @Column(nullable = false)
    private int requiredCount = 1;

    @Builder
    public TeamPosition(Team team, String positionName, int requiredCount) {
        this.team = team;
        this.positionName = positionName;
        this.requiredCount = requiredCount;
    }
}
