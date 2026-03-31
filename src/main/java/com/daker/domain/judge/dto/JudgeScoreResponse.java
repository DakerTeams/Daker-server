package com.daker.domain.judge.dto;

import com.daker.domain.judge.domain.JudgeEvaluation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JudgeScoreResponse {

    private final Long reviewId;
    private final Long hackathonId;
    private final Long teamId;
    private final Long submissionId;
    private final double totalScore;
    private final LocalDateTime reviewedAt;

    public JudgeScoreResponse(JudgeEvaluation evaluation) {
        this.reviewId = evaluation.getId();
        this.hackathonId = evaluation.getHackathon().getId();
        this.teamId = evaluation.getTeam().getId();
        this.submissionId = null; // submissions 도메인 구현 후 연결
        this.totalScore = evaluation.getTotalScore();
        this.reviewedAt = evaluation.getCreatedAt();
    }
}
