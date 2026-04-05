package com.daker.domain.hackathon.controller;

import com.daker.domain.hackathon.domain.HackathonStatus;
import com.daker.domain.hackathon.dto.*;
import com.daker.domain.hackathon.service.HackathonService;
import com.daker.domain.submission.dto.SubmissionCreateResponse;
import com.daker.domain.submission.dto.SubmissionHistoryResponse;
import com.daker.domain.submission.dto.SubmissionStatusResponse;
import com.daker.domain.submission.service.SubmissionService;
import jakarta.validation.Valid;
import com.daker.domain.team.dto.TeamSummaryResponse;
import com.daker.domain.team.service.TeamService;
import com.daker.global.response.ApiResponse;
import com.daker.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/hackathons")
@RequiredArgsConstructor
public class HackathonController {

    private final HackathonService hackathonService;
    private final TeamService teamService;
    private final SubmissionService submissionService;

    @GetMapping
    public ApiResponse<PageResponse<HackathonSummaryResponse>> getHackathons(
            @RequestParam(required = false) HackathonStatus status,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "0") int size,
            @RequestParam(defaultValue = "20") int limit
    ) {
        int pageSize = size > 0 ? size : limit;
        int pageIndex = Math.max(0, page - 1);
        return ApiResponse.ok(hackathonService.getHackathons(status, tag, q, PageRequest.of(pageIndex, pageSize)));
    }

    @GetMapping("/{id}")
    public ApiResponse<HackathonDetailResponse> getHackathon(@PathVariable Long id) {
        return ApiResponse.ok(hackathonService.getHackathon(id));
    }

    @PostMapping("/{id}/register")
    public ApiResponse<RegistrationStatusResponse> register(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RegistrationRequest request
    ) {
        return ApiResponse.ok(hackathonService.register(id, userId, request));
    }

    @GetMapping("/{id}/register")
    public ApiResponse<RegistrationStatusResponse> getRegistrationStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(hackathonService.getRegistrationStatus(id, userId));
    }

    @DeleteMapping("/{id}/register")
    public ApiResponse<java.util.Map<String, String>> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        hackathonService.cancelRegistration(id, userId);
        return ApiResponse.ok(java.util.Map.of("message", "참가 신청이 취소되었습니다."));
    }

    @GetMapping("/{id}/teams")
    public ApiResponse<PageResponse<TeamSummaryResponse>> getHackathonTeams(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(teamService.getTeams(id, null, null, PageRequest.of(page - 1, limit)));
    }

    @GetMapping("/{id}/leaderboard")
    public ApiResponse<LeaderboardResponse> getLeaderboard(@PathVariable Long id) {
        return ApiResponse.ok(hackathonService.getLeaderboard(id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{id}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SubmissionCreateResponse> submit(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @RequestPart(required = false) MultipartFile file,
            @RequestParam(required = false) String memo,
            @RequestParam Long teamId
    ) {
        return ApiResponse.created(submissionService.submit(id, userId, file, memo, teamId));
    }

    @GetMapping("/{id}/submissions/me")
    public ApiResponse<SubmissionStatusResponse> getMySubmissionStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(submissionService.getMySubmissionStatus(id, userId));
    }

    @GetMapping("/{id}/submissions/me/history")
    public ApiResponse<java.util.List<SubmissionHistoryResponse>> getMySubmissionHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(submissionService.getMySubmissionHistory(id, userId));
    }

    @PatchMapping("/{id}/submissions/{submissionId}/latest")
    public ApiResponse<SubmissionStatusResponse> activateVersion(
            @PathVariable Long id,
            @PathVariable Long submissionId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(submissionService.activateVersion(id, submissionId, userId));
    }

    @DeleteMapping("/{id}/submissions/me")
    public ApiResponse<java.util.Map<String, String>> deleteMySubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        submissionService.deleteMySubmission(id, userId);
        return ApiResponse.ok(java.util.Map.of("message", "제출이 취소되었습니다."));
    }
}
