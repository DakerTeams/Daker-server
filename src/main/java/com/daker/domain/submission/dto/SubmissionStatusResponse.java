package com.daker.domain.submission.dto;

import com.daker.domain.submission.domain.Submission;
import com.daker.domain.submission.domain.SubmissionItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SubmissionStatusResponse {

    private final boolean submitted;
    private final Long submissionId;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime submittedAt;
    private final int revision;
    private final boolean canResubmit;
    private final String memo;

    // 제출한 경우 (버전 관리 → 항상 재제출 가능)
    public SubmissionStatusResponse(Submission submission, List<SubmissionItem> items) {
        this.submitted = true;
        this.submissionId = submission.getId();
        this.teamId = submission.getTeam().getId();
        this.teamName = submission.getTeam().getName();
        this.submittedAt = submission.getSubmittedAt();
        this.revision = submission.getRevisionNo();
        this.canResubmit = true;
        this.memo = items.stream()
                .filter(item -> item.getValueText() != null)
                .map(SubmissionItem::getValueText)
                .findFirst()
                .orElse(null);
    }

    // 미제출인 경우
    public static SubmissionStatusResponse notSubmitted() {
        return new SubmissionStatusResponse();
    }

    private SubmissionStatusResponse() {
        this.submitted = false;
        this.submissionId = null;
        this.teamId = null;
        this.teamName = null;
        this.submittedAt = null;
        this.revision = 0;
        this.canResubmit = true;
        this.memo = null;
    }
}
