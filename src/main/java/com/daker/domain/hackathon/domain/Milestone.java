package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private LocalDateTime date;

    @Builder
    public Milestone(Hackathon hackathon, String title, String description, LocalDateTime date) {
        this.hackathon = hackathon;
        this.title = title;
        this.description = description;
        this.date = date;
    }
}
