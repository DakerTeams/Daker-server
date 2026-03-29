package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hackathon_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HackathonLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    private String linkType;

    private String label;

    @Column(nullable = false)
    private String url;

    @Builder
    public HackathonLink(Hackathon hackathon, String linkType, String label, String url) {
        this.hackathon = hackathon;
        this.linkType = linkType;
        this.label = label;
        this.url = url;
    }
}
