package com.daker.domain.user.controller;

import com.daker.domain.hackathon.dto.HackathonSummaryResponse;
import com.daker.domain.user.dto.UserTagResponse;
import com.daker.domain.user.service.UserTagService;
import com.daker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserTagService userTagService;

    // 내 태그 목록 조회
    @GetMapping("/me/tags")
    public ApiResponse<List<UserTagResponse>> getMyTags(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userTagService.getTags(userId));
    }

    // 태그 추가
    @PostMapping("/me/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserTagResponse> addTag(
            @AuthenticationPrincipal Long userId,
            @RequestBody Map<String, String> body) {
        return ApiResponse.created(userTagService.addTag(userId, body.get("name")));
    }

    // 태그 삭제
    @DeleteMapping("/me/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTag(@AuthenticationPrincipal Long userId, @PathVariable Long tagId) {
        userTagService.removeTag(userId, tagId);
    }

    // 관심 해커톤 (내 태그와 매칭되는 해커톤)
    @GetMapping("/me/recommended-hackathons")
    public ApiResponse<List<HackathonSummaryResponse>> getRecommendedHackathons(
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(userTagService.getRecommendedHackathons(userId));
    }
}
