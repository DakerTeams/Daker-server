package com.daker.domain.hackathon.dto;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import com.daker.domain.hackathon.domain.ScoreType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class HackathonSummaryResponse {

    private final Long id;
    private final String title;
    private final String summary;
    private final String organizer;
    private final HackathonStatus status;
    private final ScoreType scoreType;
    private final boolean votingOpen;
    private final List<String> tags;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final int participants;

    public HackathonSummaryResponse(Hackathon hackathon, List<String> tags, int participants) {
        this.id = hackathon.getId();
        this.title = hackathon.getTitle();
        this.summary = hackathon.getSummary();
        this.organizer = hackathon.getOrganizer();
        this.status = hackathon.getStatus();
        this.scoreType = hackathon.getScoreType();
        this.votingOpen = hackathon.isVotingOpen();
        this.tags = tags;
        this.startDate = hackathon.getStartDate();
        this.endDate = hackathon.getEndDate();
        this.participants = participants;
    }
}
