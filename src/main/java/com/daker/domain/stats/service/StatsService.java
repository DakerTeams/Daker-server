package com.daker.domain.stats.service;

import com.daker.domain.hackathon.domain.HackathonStatus;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.hackathon.repository.PrizeRepository;
import com.daker.domain.stats.dto.StatsResponse;
import com.daker.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final PrizeRepository prizeRepository;

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        long participants = userRepository.count();
        long activeHackathons = hackathonRepository.countByStatusAndDeletedFalse(HackathonStatus.OPEN);
        long totalPrize = prizeRepository.sumAllAmounts();

        return new StatsResponse(participants, activeHackathons, totalPrize);
    }
}