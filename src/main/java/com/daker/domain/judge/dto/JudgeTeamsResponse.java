package com.daker.domain.judge.dto;

import com.daker.domain.hackathon.domain.Criteria;
import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.judge.domain.JudgeEvaluation;
import com.daker.domain.submission.domain.Submission;
import com.daker.domain.team.domain.Team;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class JudgeTeamsResponse {

    private final Long hackathonId;
    private final String hackathonName;
    private final String scoreType;
    private final List<CriteriaInfo> criteria;
    private final List<TeamItem> items;
    private final long total;

    public JudgeTeamsResponse(Hackathon hackathon, List<TeamItem> items) {
        this.hackathonId = hackathon.getId();
        this.hackathonName = hackathon.getTitle();
        this.scoreType = hackathon.getScoreType() != null ? hackathon.getScoreType().name() : null;
        this.criteria = hackathon.getCriteriaList().stream().map(CriteriaInfo::new).toList();
        this.items = items;
        this.total = items.size();
    }

    @Getter
    public static class CriteriaInfo {
        private final String label;
        private final double maxScore;

        public CriteriaInfo(Criteria criteria) {
            this.label = criteria.getName();
            this.maxScore = criteria.getMaxScore();
        }
    }

    @Getter
    public static class TeamItem {
        private final Long teamId;
        private final String teamName;
        private final Long submissionId;
        private final LocalDateTime submittedAt;
        private final String reviewStatus;
        private final List<Double> scores;
        private final Double totalScore;

        private static final ObjectMapper objectMapper = new ObjectMapper();

        public TeamItem(Team team, Submission submission, JudgeEvaluation evaluation) {
            this.teamId = team.getId();
            this.teamName = team.getName();
            this.submissionId = submission != null ? submission.getId() : null;
            this.submittedAt = submission != null ? submission.getSubmittedAt() : null;

            if (evaluation != null) {
                this.reviewStatus = "reviewed";
                this.totalScore = evaluation.getTotalScore();
                this.scores = parseScores(evaluation.getScoresJson());
            } else {
                this.reviewStatus = "pending";
                this.totalScore = null;
                this.scores = null;
            }
        }

        private List<Double> parseScores(String json) {
            if (json == null) return null;
            try {
                return objectMapper.readValue(json, new TypeReference<>() {});
            } catch (Exception e) {
                return null;
            }
        }
    }
}
