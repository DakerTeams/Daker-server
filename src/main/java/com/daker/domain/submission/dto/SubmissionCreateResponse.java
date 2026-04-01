package com.daker.domain.submission.dto;

import com.daker.domain.submission.domain.Submission;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SubmissionCreateResponse {

    private final Long submissionId;
    private final Long hackathonId;
    private final Long teamId;
    private final LocalDateTime submittedAt;
    private final int revision;

    public SubmissionCreateResponse(Submission submission) {
        this.submissionId = submission.getId();
        this.hackathonId = submission.getHackathon().getId();
        this.teamId = submission.getTeam().getId();
        this.submittedAt = submission.getSubmittedAt();
        this.revision = submission.getRevisionNo();
    }
}
