package com.daker.domain.submission.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminSubmissionHackathonSummaryResponse {

    private final Long hackathonId;
    private final String hackathonName;
    private final int submittedTeamCount;
    private final int totalFileCount;
    private final LocalDateTime latestSubmittedAt;
}
