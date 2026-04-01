package com.daker.domain.admin.controller;

import com.daker.domain.admin.dto.*;
import com.daker.domain.admin.service.AdminService;
import com.daker.domain.submission.dto.AdminSubmissionResponse;
import com.daker.domain.submission.service.SubmissionService;
import com.daker.domain.user.domain.Role;
import com.daker.global.response.ApiResponse;
import com.daker.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final SubmissionService submissionService;

    // -------------------------------------------------------------------------
    // 대시보드
    // -------------------------------------------------------------------------

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(adminService.getDashboard(page, limit));
    }

    // -------------------------------------------------------------------------
    // 해커톤 관리
    // -------------------------------------------------------------------------

    @GetMapping("/hackathons")
    public ApiResponse<java.util.Map<String, Object>> getHackathons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(java.util.Map.of("hackathonList",
                adminService.getHackathons(PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id")))));
    }

    @PostMapping("/hackathons")
    public ApiResponse<AdminHackathonCreateResponse> createHackathon(
            @RequestBody @Valid HackathonCreateRequest request
    ) {
        return ApiResponse.ok(adminService.createHackathon(request));
    }

    @PatchMapping("/hackathons/{hackathonId}")
    public ApiResponse<AdminHackathonUpdateResponse> updateHackathon(
            @PathVariable Long hackathonId,
            @RequestBody HackathonUpdateRequest request
    ) {
        return ApiResponse.ok(adminService.updateHackathon(hackathonId, request));
    }

    @PatchMapping("/hackathons/{hackathonId}/close")
    public ApiResponse<AdminHackathonCloseResponse> closeHackathon(@PathVariable Long hackathonId) {
        return ApiResponse.ok(adminService.closeHackathon(hackathonId));
    }

    // -------------------------------------------------------------------------
    // 유저 관리
    // -------------------------------------------------------------------------

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(adminService.getUsers(role,
                PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id"))));
    }

    // -------------------------------------------------------------------------
    // 심사위원 관리
    // -------------------------------------------------------------------------

    @GetMapping("/judges")
    public ApiResponse<PageResponse<AdminJudgeResponse>> getJudges(
            @RequestParam(required = false) Long hackathonId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(adminService.getJudges(hackathonId,
                PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id"))));
    }

    // -------------------------------------------------------------------------
    // 제출물 관리
    // -------------------------------------------------------------------------

    @GetMapping("/submissions")
    public ApiResponse<PageResponse<AdminSubmissionResponse>> getSubmissions(
            @RequestParam(required = false) Long hackathonId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(submissionService.getAdminSubmissions(hackathonId, teamId,
                PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id"))));
    }

    @PatchMapping("/users/{userId}/judges")
    public ApiResponse<AdminUserResponse> updateJudgeRole(
            @PathVariable Long userId,
            @RequestBody @Valid JudgeRoleRequest request
    ) {
        return ApiResponse.ok(adminService.updateJudgeRole(userId, request));
    }

    @PostMapping("/hackathons/{hackathonId}/judges/{userId}")
    public ApiResponse<Void> assignJudge(
            @PathVariable Long hackathonId,
            @PathVariable Long userId
    ) {
        adminService.assignJudge(hackathonId, userId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/hackathons/{hackathonId}/judges/{userId}")
    public ApiResponse<Void> removeJudge(
            @PathVariable Long hackathonId,
            @PathVariable Long userId
    ) {
        adminService.removeJudge(hackathonId, userId);
        return ApiResponse.ok(null);
    }
}
