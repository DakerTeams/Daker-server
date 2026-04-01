package com.daker.domain.submission.dto;

import com.daker.domain.submission.domain.Submission;
import com.daker.domain.submission.domain.SubmissionItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminSubmissionResponse {

    private final Long submissionId;
    private final Long hackathonId;
    private final String hackathonName;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime submittedAt;
    private final int revisionNo;
    private final String reviewStatus;
    private final List<SubmissionItemSummary> submissionItems;

    public AdminSubmissionResponse(Submission submission, List<SubmissionItem> items, boolean reviewed) {
        this.submissionId = submission.getId();
        this.hackathonId = submission.getHackathon().getId();
        this.hackathonName = submission.getHackathon().getTitle();
        this.teamId = submission.getTeam().getId();
        this.teamName = submission.getTeam().getName();
        this.submittedAt = submission.getSubmittedAt();
        this.revisionNo = submission.getRevisionNo();
        this.reviewStatus = reviewed ? "reviewed" : "pending";
        this.submissionItems = items.stream()
                .filter(item -> item.getFileName() != null || item.getValueUrl() != null)
                .map(SubmissionItemSummary::new)
                .toList();
    }

    @Getter
    public static class SubmissionItemSummary {
        private final String fileName;
        private final String valueUrl;

        public SubmissionItemSummary(SubmissionItem item) {
            this.fileName = item.getFileName();
            this.valueUrl = item.getValueUrl();
        }
    }
}
