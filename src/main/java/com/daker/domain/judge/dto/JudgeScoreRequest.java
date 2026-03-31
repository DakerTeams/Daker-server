package com.daker.domain.judge.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class JudgeScoreRequest {

    private Long submissionId;

    private List<ScoreItem> scores;

    @Getter
    public static class ScoreItem {
        private String label;
        private double score;
    }
}
