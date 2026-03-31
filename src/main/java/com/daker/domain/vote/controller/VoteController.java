package com.daker.domain.vote.controller;

import com.daker.domain.vote.dto.VoteRequest;
import com.daker.domain.vote.dto.VoteResponse;
import com.daker.domain.vote.service.VoteService;
import com.daker.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hackathons/{hackathonId}/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public ApiResponse<VoteResponse> vote(
            @PathVariable Long hackathonId,
            @RequestBody @Valid VoteRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(voteService.vote(hackathonId, userId, request));
    }
}
