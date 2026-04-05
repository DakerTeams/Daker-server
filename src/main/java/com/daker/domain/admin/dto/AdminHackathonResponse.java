package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import com.daker.domain.hackathon.domain.ScoreType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminHackathonResponse {

    private final Long id;
    private final String title;
    private final String summary;
    private final String description;
    private final String organizerName;
    private final HackathonStatus status;
    private final ScoreType scoreType;
    private final LocalDateTime registrationStartAt;
    private final LocalDateTime registrationEndAt;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final LocalDateTime submissionDeadlineAt;
    private final int maxTeamSize;
    private final Integer maxParticipants;
    private final boolean campEnabled;
    private final boolean allowSolo;
    private final int numOfTeams;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AdminHackathonResponse(Hackathon hackathon, int numOfTeams) {
        this.id = hackathon.getId();
        this.title = hackathon.getTitle();
        this.summary = hackathon.getSummary();
        this.description = hackathon.getDescription();
        this.organizerName = hackathon.getOrganizer();
        this.status = hackathon.getStatus();
        this.scoreType = hackathon.getScoreType();
        this.registrationStartAt = hackathon.getRegistrationStartDate();
        this.registrationEndAt = hackathon.getRegistrationEndDate();
        this.startAt = hackathon.getStartDate();
        this.endAt = hackathon.getEndDate();
        this.submissionDeadlineAt = hackathon.getSubmissionDeadlineAt();
        this.maxTeamSize = hackathon.getMaxTeamSize();
        this.maxParticipants = hackathon.getMaxParticipants();
        this.campEnabled = hackathon.isCampEnabled();
        this.allowSolo = hackathon.isAllowSolo();
        this.numOfTeams = numOfTeams;
        this.createdAt = hackathon.getCreatedAt();
        this.updatedAt = hackathon.getUpdatedAt();
    }
}
