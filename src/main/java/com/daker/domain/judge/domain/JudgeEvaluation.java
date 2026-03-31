package com.daker.domain.judge.domain;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.team.domain.Team;
import com.daker.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "judge_evaluations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hackathon_id", "team_id", "judge_user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class JudgeEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_user_id", nullable = false)
    private User judge;

    @Column(nullable = false)
    private double totalScore;

    @Column(columnDefinition = "TEXT")
    private String scoresJson;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public JudgeEvaluation(Hackathon hackathon, Team team, User judge, double totalScore, String scoresJson) {
        this.hackathon = hackathon;
        this.team = team;
        this.judge = judge;
        this.totalScore = totalScore;
        this.scoresJson = scoresJson;
    }

    public void update(double totalScore, String scoresJson) {
        this.totalScore = totalScore;
        this.scoresJson = scoresJson;
    }
}
