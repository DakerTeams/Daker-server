package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminDashboardResponse {

    private final HackathonStats hackathons;
    private final TeamStats participatedTeams;
    private final UserStats users;
    private final SubmissionStats submissions;
    private final LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class HackathonStats {
        private final long total;
        private final long active;
        private final long upcoming;
        private final long closed;
        private final long ended;
        private final long newThisMonth;
        private final HackathonList hackathonList;
    }

    @Getter
    @Builder
    public static class HackathonList {
        private final List<HackathonItem> items;
        private final long totalCount;
        private final int page;
        private final int limit;
    }

    @Getter
    public static class HackathonItem {
        private final Long id;
        private final String title;
        private final HackathonStatus status;
        private final int numOfTeams;
        private final int numOfSubmissions;
        private final LocalDateTime submissionDeadlineAt;

        public HackathonItem(Hackathon hackathon, int numOfTeams) {
            this.id = hackathon.getId();
            this.title = hackathon.getTitle();
            this.status = hackathon.getStatus();
            this.numOfTeams = numOfTeams;
            this.numOfSubmissions = 0; // TODO: 제출 도메인 개발 후 연결
            this.submissionDeadlineAt = hackathon.getSubmissionDeadlineAt();
        }
    }

    @Getter
    @Builder
    public static class TeamStats {
        private final long total;
        private final long newThisWeek;
    }

    @Getter
    @Builder
    public static class UserStats {
        private final long total;
        private final long newThisMonth;
        private final long judges;
    }

    @Getter
    @Builder
    public static class SubmissionStats {
        private final long total;
        private final long pendingReview;
    }
}
