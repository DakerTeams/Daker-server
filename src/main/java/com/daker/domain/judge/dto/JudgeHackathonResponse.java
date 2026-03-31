package com.daker.domain.judge.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class JudgeHackathonResponse {

    private final List<HackathonItem> items;
    private final long totalCount;

    public JudgeHackathonResponse(List<HackathonItem> items) {
        this.items = items;
        this.totalCount = items.size();
    }

    @Getter
    public static class HackathonItem {
        private final Long id;
        private final String title;
        private final String status;
        private final String scoreType;
        private final long submissionCount;
        private final long reviewedCount;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;

        public HackathonItem(Hackathon hackathon, long reviewedCount) {
            this.id = hackathon.getId();
            this.title = hackathon.getTitle();
            this.status = hackathon.getStatus().name();
            this.scoreType = hackathon.getScoreType() != null ? hackathon.getScoreType().name() : null;
            this.submissionCount = 0; // submissions 도메인 구현 후 연결
            this.reviewedCount = reviewedCount;
            this.startDate = hackathon.getStartDate();
            this.endDate = hackathon.getEndDate();
        }
    }
}
