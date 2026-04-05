package com.daker.global.scheduler;

import com.daker.domain.hackathon.repository.HackathonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HackathonStatusScheduler {

    private final HackathonRepository hackathonRepository;

    @Scheduled(cron = "0 * * * * *") // 매분 0초마다 실행
    @Transactional
    public void updateHackathonStatuses() {
        LocalDateTime now = LocalDateTime.now();

        int upcoming = hackathonRepository.updateToUpcoming(now);
        int open     = hackathonRepository.updateToOpen(now);
        int closed   = hackathonRepository.updateToClosed(now);
        int ended    = hackathonRepository.updateToEnded(now);

        int total = upcoming + open + closed + ended;
        if (total > 0) {
            log.info("[HackathonScheduler] status 갱신: UPCOMING={}, OPEN={}, CLOSED={}, ENDED={}",
                    upcoming, open, closed, ended);
        }
    }
}
