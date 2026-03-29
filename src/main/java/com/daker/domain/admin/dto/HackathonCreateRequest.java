package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.ScoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class HackathonCreateRequest {

    @NotBlank
    private String title;

    private String summary;

    private String description;

    private String thumbnailUrl;

    @NotBlank
    private String organizerName;

    @NotNull
    private ScoreType scoreType;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    @NotNull
    private LocalDateTime registrationStartAt;

    @NotNull
    private LocalDateTime registrationEndAt;

    private LocalDateTime submissionDeadlineAt;

    @NotNull
    private Integer maxTeamSize;

    private Integer maxParticipants;

    private boolean campEnabled;

    private boolean allowSolo;

    private List<String> tags;

    private List<NoticeRequest> notices;

    private List<LinkRequest> links;

    private List<PrizeRequest> prizes;

    private List<CriteriaRequest> criteria;

    private List<MilestoneRequest> milestones;

    @Getter
    public static class NoticeRequest {
        private String content;
    }

    @Getter
    public static class LinkRequest {
        private String linkType;
        private String label;
        private String url;
    }

    @Getter
    public static class PrizeRequest {
        private int rank;
        private String label;
        private String amount;
    }

    @Getter
    public static class CriteriaRequest {
        private String label;
        private String weight;
        private Double maxScore;
    }

    @Getter
    public static class MilestoneRequest {
        private String label;
        private LocalDateTime date;
    }
}
