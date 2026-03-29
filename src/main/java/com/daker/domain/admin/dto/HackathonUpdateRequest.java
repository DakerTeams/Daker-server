package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.ScoreType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class HackathonUpdateRequest {

    private String title;
    private String summary;
    private String description;
    private String thumbnailUrl;
    private String organizerName;
    private ScoreType scoreType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime registrationStartAt;
    private LocalDateTime registrationEndAt;
    private LocalDateTime submissionDeadlineAt;
    private Integer maxTeamSize;
    private Integer maxParticipants;
    private Boolean campEnabled;
    private Boolean allowSolo;
    private List<String> tags;
    private List<HackathonCreateRequest.NoticeRequest> notices;
    private List<HackathonCreateRequest.LinkRequest> links;
    private List<HackathonCreateRequest.PrizeRequest> prizes;
    private List<HackathonCreateRequest.CriteriaRequest> criteria;
    private List<HackathonCreateRequest.MilestoneRequest> milestones;
}
