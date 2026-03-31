package com.daker.domain.hackathon.dto;

import com.daker.domain.hackathon.domain.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class HackathonDetailResponse {

    private final Long id;
    private final String title;
    private final String desc;
    private final String organizer;
    private final HackathonStatus status;
    private final ScoreType scoreType;
    private final List<String> tags;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final LocalDateTime registrationStartDate;
    private final LocalDateTime registrationEndDate;
    private final int maxTeamSize;
    private final int participants;
    private final List<MilestoneInfo> milestones;
    private final List<PrizeInfo> prizes;
    private final List<CriteriaInfo> criteria;

    public HackathonDetailResponse(Hackathon hackathon, List<String> tags, int participants) {
        this.id = hackathon.getId();
        this.title = hackathon.getTitle();
        this.desc = hackathon.getDescription();
        this.organizer = hackathon.getOrganizer();
        this.status = hackathon.getStatus();
        this.scoreType = hackathon.getScoreType();
        this.tags = tags;
        this.startDate = hackathon.getStartDate();
        this.endDate = hackathon.getEndDate();
        this.registrationStartDate = hackathon.getRegistrationStartDate();
        this.registrationEndDate = hackathon.getRegistrationEndDate();
        this.maxTeamSize = hackathon.getMaxTeamSize();
        this.participants = participants;
        this.milestones = hackathon.getMilestones().stream().map(MilestoneInfo::new).toList();
        this.prizes = hackathon.getPrizes().stream().map(PrizeInfo::new).toList();
        this.criteria = hackathon.getCriteriaList().stream().map(CriteriaInfo::new).toList();
    }

    @Getter
    public static class MilestoneInfo {
        private final String title;
        private final String description;
        private final LocalDateTime date;

        public MilestoneInfo(Milestone milestone) {
            this.title = milestone.getTitle();
            this.description = milestone.getDescription();
            this.date = milestone.getDate();
        }
    }

    @Getter
    public static class PrizeInfo {
        private final int ranking;
        private final int amount;
        private final String description;

        public PrizeInfo(Prize prize) {
            this.ranking = prize.getRanking();
            this.amount = prize.getAmount();
            this.description = prize.getDescription();
        }
    }

    @Getter
    public static class CriteriaInfo {
        private final Long id;
        private final String name;
        private final String description;
        private final double maxScore;

        public CriteriaInfo(Criteria criteria) {
            this.id = criteria.getId();
            this.name = criteria.getName();
            this.description = criteria.getDescription();
            this.maxScore = criteria.getMaxScore();
        }
    }
}
