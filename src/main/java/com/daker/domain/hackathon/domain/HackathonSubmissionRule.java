package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hackathon_submission_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HackathonSubmissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @Column(nullable = false)
    private String artifactType; // text, url, pdf, zip

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private String label;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    public HackathonSubmissionRule(Hackathon hackathon, String artifactType, boolean required,
                                    String label, String description, int sortOrder) {
        this.hackathon = hackathon;
        this.artifactType = artifactType;
        this.required = required;
        this.label = label;
        this.description = description;
        this.sortOrder = sortOrder;
    }
}
