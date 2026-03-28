package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "criteria")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private double maxScore;

    @Builder
    public Criteria(Hackathon hackathon, String name, String description, double maxScore) {
        this.hackathon = hackathon;
        this.name = name;
        this.description = description;
        this.maxScore = maxScore;
    }
}
