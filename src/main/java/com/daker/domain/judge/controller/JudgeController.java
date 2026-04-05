package com.daker.domain.judge.controller;

import com.daker.domain.judge.dto.JudgeHackathonResponse;
import com.daker.domain.judge.dto.JudgeScoreRequest;
import com.daker.domain.judge.dto.JudgeScoreResponse;
import com.daker.domain.judge.dto.JudgeSubmissionResponse;
import com.daker.domain.judge.dto.JudgeTeamsResponse;
import com.daker.domain.judge.service.JudgeService;
import com.daker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/judges")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('JUDGE', 'ADMIN')")
public class JudgeController {

    private final JudgeService judgeService;

    @GetMapping("/hackathons")
    public ApiResponse<JudgeHackathonResponse> getAssignedHackathons(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(judgeService.getAssignedHackathons(userId));
    }

    @GetMapping("/hackathons/{hackathonId}/teams")
    public ApiResponse<JudgeTeamsResponse> getTeams(
            @PathVariable Long hackathonId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(judgeService.getTeamsForHackathon(hackathonId, userId));
    }

    @GetMapping("/hackathons/{hackathonId}/teams/{teamId}/submission")
    public ApiResponse<JudgeSubmissionResponse> getSubmission(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(judgeService.getSubmissionForTeam(hackathonId, teamId, userId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hackathons/{hackathonId}/teams/{teamId}")
    public ApiResponse<JudgeScoreResponse> score(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody JudgeScoreRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.created(judgeService.score(hackathonId, teamId, userId, request));
    }
}
