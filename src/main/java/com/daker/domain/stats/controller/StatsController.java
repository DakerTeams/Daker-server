package com.daker.domain.stats.controller;

import com.daker.domain.stats.dto.StatsResponse;
import com.daker.domain.stats.service.StatsService;
import com.daker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ApiResponse<StatsResponse> getStats() {
        return ApiResponse.ok(statsService.getStats());
    }
}