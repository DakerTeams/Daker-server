package com.daker.domain.hackathon.domain;

import com.daker.domain.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "hackathon_registrations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class HackathonRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false, unique = true)
    private Team team;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Builder
    public HackathonRegistration(Hackathon hackathon, Team team) {
        this.hackathon = hackathon;
        this.team = team;
    }
}
