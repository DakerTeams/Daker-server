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

    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;

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

    private LocalDateTime submissionDeadlineAt;

    private LocalDateTime closedAt;

    @Column(nullable = false)
    private int maxTeamSize;

    private Integer maxParticipants;

    @Column(nullable = false)
    private boolean campEnabled = false;

    @Column(nullable = false)
    private boolean allowSolo = false;

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

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HackathonNotice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HackathonLink> links = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Hackathon(String title, String summary, String description, String thumbnailUrl,
                     String organizer, HackathonStatus status, ScoreType scoreType,
                     LocalDateTime startDate, LocalDateTime endDate,
                     LocalDateTime registrationStartDate, LocalDateTime registrationEndDate,
                     LocalDateTime submissionDeadlineAt,
                     int maxTeamSize, Integer maxParticipants,
                     boolean campEnabled, boolean allowSolo) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.organizer = organizer;
        this.status = status;
        this.scoreType = scoreType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.submissionDeadlineAt = submissionDeadlineAt;
        this.maxTeamSize = maxTeamSize;
        this.maxParticipants = maxParticipants;
        this.campEnabled = campEnabled;
        this.allowSolo = allowSolo;
    }

    public boolean isRegistrationOpen() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(registrationStartDate) && now.isBefore(registrationEndDate);
    }

    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean isVotingOpen() {
        if (submissionDeadlineAt == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(submissionDeadlineAt) && now.isBefore(endDate);
    }

    public void update(String title, String summary, String description, String thumbnailUrl,
                       String organizer, ScoreType scoreType,
                       LocalDateTime startDate, LocalDateTime endDate,
                       LocalDateTime registrationStartDate, LocalDateTime registrationEndDate,
                       LocalDateTime submissionDeadlineAt,
                       int maxTeamSize, Integer maxParticipants,
                       boolean campEnabled, boolean allowSolo) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.organizer = organizer;
        this.scoreType = scoreType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.submissionDeadlineAt = submissionDeadlineAt;
        this.maxTeamSize = maxTeamSize;
        this.maxParticipants = maxParticipants;
        this.campEnabled = campEnabled;
        this.allowSolo = allowSolo;
    }

    public void updateStatus(HackathonStatus status) {
        this.status = status;
        if (status == HackathonStatus.CLOSED) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public void softDelete() {
        this.deleted = true;
    }
}
