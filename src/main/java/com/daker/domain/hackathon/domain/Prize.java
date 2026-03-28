package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prizes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @Column(nullable = false)
    private int ranking;

    @Column(nullable = false)
    private int amount;

    @Column
    private String description;

    @Builder
    public Prize(Hackathon hackathon, int ranking, int amount, String description) {
        this.hackathon = hackathon;
        this.ranking = ranking;
        this.amount = amount;
        this.description = description;
    }
}
