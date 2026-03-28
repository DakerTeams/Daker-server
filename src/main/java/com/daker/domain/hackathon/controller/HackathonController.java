package com.daker.domain.hackathon.controller;

import com.daker.domain.hackathon.domain.HackathonStatus;
import com.daker.domain.hackathon.dto.*;
import com.daker.domain.hackathon.service.HackathonService;
import com.daker.domain.team.dto.TeamSummaryResponse;
import com.daker.domain.team.service.TeamService;
import com.daker.global.response.ApiResponse;
import com.daker.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hackathons")
@RequiredArgsConstructor
public class HackathonController {

    private final HackathonService hackathonService;
    private final TeamService teamService;

    @GetMapping
    public ApiResponse<PageResponse<HackathonSummaryResponse>> getHackathons(
            @RequestParam(required = false) HackathonStatus status,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(hackathonService.getHackathons(status, tag, q, PageRequest.of(page - 1, limit)));
    }

    @GetMapping("/{id}")
    public ApiResponse<HackathonDetailResponse> getHackathon(@PathVariable Long id) {
        return ApiResponse.ok(hackathonService.getHackathon(id));
    }

    @GetMapping("/{id}/register")
    public ApiResponse<RegistrationStatusResponse> getRegistrationStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(hackathonService.getRegistrationStatus(id, userId));
    }

    @DeleteMapping("/{id}/register")
    public ApiResponse<Void> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        hackathonService.cancelRegistration(id, userId);
        return ApiResponse.ok(null);
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
}
