package com.daker.domain.ranking.service;

import com.daker.domain.ranking.dto.ParticipationRankingResponse;
import com.daker.domain.ranking.dto.RankingPeriod;
import com.daker.domain.ranking.dto.ScoreRankingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RankingService {

    @Transactional(readOnly = true)
    public List<ScoreRankingResponse> getScoreRankings(RankingPeriod period, Long userId) {
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<ParticipationRankingResponse> getParticipationRankings(RankingPeriod period, Long userId) {
        return List.of();
    }
}
