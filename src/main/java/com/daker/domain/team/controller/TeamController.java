package com.daker.domain.team.controller;

import com.daker.domain.team.dto.*;
import com.daker.domain.team.service.TeamService;
import com.daker.global.response.ApiResponse;
import com.daker.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ApiResponse<PageResponse<TeamSummaryResponse>> getTeams(
            @RequestParam(required = false) Long hackathonId,
            @RequestParam(required = false) Boolean isOpen,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(teamService.getTeams(hackathonId, isOpen, q, PageRequest.of(page - 1, limit)));
    }

    @GetMapping("/me")
    public ApiResponse<List<TeamSummaryResponse>> getMyTeams(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long hackathonId
    ) {
        return ApiResponse.ok(teamService.getMyTeams(userId, hackathonId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TeamDetailResponse> getTeam(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(teamService.getTeam(id, userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TeamDetailResponse> createTeam(
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.created(teamService.createTeam(request, userId));
    }

    @PatchMapping("/{id}")
    public ApiResponse<TeamDetailResponse> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(teamService.updateTeam(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<java.util.Map<String, String>> deleteTeam(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        teamService.deleteTeam(id, userId);
        return ApiResponse.ok(java.util.Map.of("message", "팀 모집글이 삭제되었습니다."));
    }

    @PostMapping("/{id}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> apply(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        String position = body != null ? body.get("position") : null;
        teamService.apply(id, userId, position);
        return ApiResponse.created(null);
    }

    @GetMapping("/{id}/applications")
    public ApiResponse<List<TeamApplicationResponse>> getApplications(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(teamService.getApplications(id, userId));
    }

    @PatchMapping("/{id}/applications/{appId}")
    public ApiResponse<TeamApplicationResponse> decideApplication(
            @PathVariable Long id,
            @PathVariable Long appId,
            @Valid @RequestBody ApplicationDecisionRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(teamService.decideApplication(id, appId, request, userId));
    }
}
