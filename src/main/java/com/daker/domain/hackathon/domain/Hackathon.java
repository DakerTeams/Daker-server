package com.daker.domain.hackathon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hackathons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String organizer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HackathonStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScoreType scoreType;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime registrationStartDate;

    @Column(nullable = false)
    private LocalDateTime registrationEndDate;

    @Column(nullable = false)
    private int maxTeamSize;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HackathonTag> hackathonTags = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prize> prizes = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Criteria> criteriaList = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Hackathon(String title, String description, String organizer,
                     HackathonStatus status, ScoreType scoreType,
                     LocalDateTime startDate, LocalDateTime endDate,
                     LocalDateTime registrationStartDate, LocalDateTime registrationEndDate,
                     int maxTeamSize) {
        this.title = title;
        this.description = description;
        this.organizer = organizer;
        this.status = status;
        this.scoreType = scoreType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.maxTeamSize = maxTeamSize;
    }

    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(registrationStartDate) && now.isBefore(registrationEndDate);
    }

    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public void updateStatus(HackathonStatus status) {
        this.status = status;
    }

    public void softDelete() {
        this.deleted = true;
    }
}
