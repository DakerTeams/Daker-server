package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hackathon_notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HackathonNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public HackathonNotice(Hackathon hackathon, String content) {
        this.hackathon = hackathon;
        this.content = content;
    }
}
