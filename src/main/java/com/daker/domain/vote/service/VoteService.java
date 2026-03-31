package com.daker.domain.vote.service;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonRegistration;
import com.daker.domain.hackathon.domain.ScoreType;
import com.daker.domain.hackathon.repository.HackathonRegistrationRepository;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.domain.vote.domain.Vote;
import com.daker.domain.vote.dto.VoteRequest;
import com.daker.domain.vote.dto.VoteResponse;
import com.daker.domain.vote.repository.VoteRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final HackathonRepository hackathonRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;

    @Transactional
    public VoteResponse vote(Long hackathonId, Long userId, VoteRequest request) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        if (hackathon.getScoreType() != ScoreType.VOTE) {
            throw new CustomException(ErrorCode.SCORE_TYPE_MISMATCH);
        }

        if (!hackathon.isVotingOpen()) {
            throw new CustomException(ErrorCode.VOTE_PERIOD_INVALID);
        }

        if (voteRepository.existsByHackathonIdAndVoterId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_VOTED);
        }

        Long teamId = request.getTeamId();

        // 자기 팀 투표 방지
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.CANNOT_VOTE_OWN_TEAM);
        }

        // 해당 팀이 이 해커톤에 등록된 팀인지 확인
        HackathonRegistration registration = hackathonRegistrationRepository
                .findByHackathonIdAndTeamId(hackathonId, teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        User voter = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Team team = registration.getTeam();

        Vote vote = voteRepository.save(Vote.builder()
                .hackathon(hackathon)
                .voter(voter)
                .team(team)
                .build());

        return new VoteResponse(vote);
    }
}
