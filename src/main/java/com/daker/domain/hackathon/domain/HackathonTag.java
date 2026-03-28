package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hackathon_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HackathonTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Builder
    public HackathonTag(Hackathon hackathon, Tag tag) {
        this.hackathon = hackathon;
        this.tag = tag;
    }
}
