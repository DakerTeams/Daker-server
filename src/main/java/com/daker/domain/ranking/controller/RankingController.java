package com.daker.domain.ranking.controller;

import com.daker.domain.ranking.dto.ParticipationRankingResponse;
import com.daker.domain.ranking.dto.RankingPeriod;
import com.daker.domain.ranking.dto.ScoreRankingResponse;
import com.daker.domain.ranking.service.RankingService;
import com.daker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ApiResponse<List<ScoreRankingResponse>> getScoreRankings(
            @RequestParam(defaultValue = "all") String period,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(rankingService.getScoreRankings(RankingPeriod.from(period), userId));
    }

    @GetMapping("/participation")
    public ApiResponse<List<ParticipationRankingResponse>> getParticipationRankings(
            @RequestParam(defaultValue = "all") String period,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(rankingService.getParticipationRankings(RankingPeriod.from(period), userId));
    }
}
