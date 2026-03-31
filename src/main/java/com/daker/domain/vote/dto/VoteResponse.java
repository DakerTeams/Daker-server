package com.daker.domain.vote.dto;

import com.daker.domain.vote.domain.Vote;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VoteResponse {

    private final Long voteId;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime votedAt;

    public VoteResponse(Vote vote) {
        this.voteId = vote.getId();
        this.teamId = vote.getTeam().getId();
        this.teamName = vote.getTeam().getName();
        this.votedAt = vote.getVotedAt();
    }
}
