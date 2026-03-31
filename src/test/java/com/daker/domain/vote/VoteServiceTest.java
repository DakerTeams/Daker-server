package com.daker.domain.vote;

import com.daker.domain.hackathon.domain.*;
import com.daker.domain.hackathon.repository.HackathonRegistrationRepository;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.domain.vote.domain.Vote;
import com.daker.domain.vote.dto.VoteRequest;
import com.daker.domain.vote.dto.VoteResponse;
import com.daker.domain.vote.repository.VoteRepository;
import com.daker.domain.vote.service.VoteService;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @InjectMocks
    private VoteService voteService;

    @Mock private HackathonRepository hackathonRepository;
    @Mock private HackathonRegistrationRepository hackathonRegistrationRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private VoteRepository voteRepository;

    // -------------------------------------------------------------------------
    // 헬퍼
    // -------------------------------------------------------------------------

    private Hackathon mockVoteHackathon(boolean votingOpen) {
        LocalDateTime now = LocalDateTime.now();
        Hackathon h = Hackathon.builder()
                .title("Vote Hackathon")
                .organizer("org")
                .status(HackathonStatus.OPEN)
                .scoreType(ScoreType.VOTE)
                .startDate(now.minusDays(5))
                .endDate(votingOpen ? now.plusDays(2) : now.minusDays(1))
                .registrationStartDate(now.minusDays(10))
                .registrationEndDate(now.minusDays(6))
                .submissionDeadlineAt(votingOpen ? now.minusDays(1) : now.plusDays(1))
                .maxTeamSize(5)
                .build();
        setField(h, "id", 1L);
        return h;
    }

    private Hackathon mockScoreHackathon() {
        LocalDateTime now = LocalDateTime.now();
        Hackathon h = Hackathon.builder()
                .title("Score Hackathon")
                .organizer("org")
                .status(HackathonStatus.OPEN)
                .scoreType(ScoreType.SCORE)
                .startDate(now.minusDays(5))
                .endDate(now.plusDays(2))
                .registrationStartDate(now.minusDays(10))
                .registrationEndDate(now.minusDays(6))
                .maxTeamSize(5)
                .build();
        setField(h, "id", 2L);
        return h;
    }

    private User mockUser(Long id) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("user" + id)
                .password("encoded")
                .role(Role.USER)
                .build();
        setField(user, "id", id);
        return user;
    }

    private Team mockTeam(Long id, Hackathon hackathon, User leader) {
        Team team = Team.builder()
                .hackathon(hackathon)
                .leader(leader)
                .name("Team " + id)
                .description("desc")
                .isOpen(true)
                .build();
        setField(team, "id", id);
        return team;
    }

    private HackathonRegistration mockRegistration(Hackathon hackathon, Team team) {
        HackathonRegistration reg = HackathonRegistration.builder()
                .hackathon(hackathon)
                .team(team)
                .build();
        setField(reg, "id", 1L);
        return reg;
    }

    private VoteRequest voteRequest(Long teamId) {
        VoteRequest req = new VoteRequest();
        setField(req, "teamId", teamId);
        return req;
    }

    // -------------------------------------------------------------------------
    // vote 성공
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("투표 성공")
    void vote_success() {
        Hackathon hackathon = mockVoteHackathon(true);
        User voter = mockUser(1L);
        User teamLeader = mockUser(2L);
        Team team = mockTeam(1L, hackathon, teamLeader);
        HackathonRegistration reg = mockRegistration(hackathon, team);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(voteRepository.existsByHackathonIdAndVoterId(1L, 1L)).willReturn(false);
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(false);
        given(hackathonRegistrationRepository.findByHackathonIdAndTeamId(1L, 1L)).willReturn(Optional.of(reg));
        given(userRepository.findById(1L)).willReturn(Optional.of(voter));
        given(voteRepository.save(any(Vote.class))).willAnswer(inv -> {
            Vote v = inv.getArgument(0);
            setField(v, "id", 100L);
            setField(v, "votedAt", LocalDateTime.now());
            return v;
        });

        VoteResponse response = voteService.vote(1L, 1L, voteRequest(1L));

        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getTeamName()).isEqualTo("Team 1");
        verify(voteRepository).save(any(Vote.class));
    }

    // -------------------------------------------------------------------------
    // vote 실패 케이스
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("해커톤이 존재하지 않으면 예외")
    void vote_hackathonNotFound() {
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> voteService.vote(99L, 1L, voteRequest(1L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    @Test
    @DisplayName("SCORE 방식 해커톤에 투표 요청 시 예외")
    void vote_scoreTypeMismatch() {
        Hackathon hackathon = mockScoreHackathon();
        given(hackathonRepository.findByIdAndDeletedFalse(2L)).willReturn(Optional.of(hackathon));

        assertThatThrownBy(() -> voteService.vote(2L, 1L, voteRequest(1L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SCORE_TYPE_MISMATCH);
    }

    @Test
    @DisplayName("투표 기간이 아닐 때 예외 (submissionDeadlineAt 이전)")
    void vote_notVotingPeriod() {
        Hackathon hackathon = mockVoteHackathon(false); // submissionDeadlineAt이 미래
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));

        assertThatThrownBy(() -> voteService.vote(1L, 1L, voteRequest(1L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.VOTE_PERIOD_INVALID);
    }

    @Test
    @DisplayName("중복 투표 시 예외")
    void vote_alreadyVoted() {
        Hackathon hackathon = mockVoteHackathon(true);
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(voteRepository.existsByHackathonIdAndVoterId(1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> voteService.vote(1L, 1L, voteRequest(1L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_VOTED);
    }

    @Test
    @DisplayName("자기 팀에 투표 시 예외")
    void vote_ownTeam() {
        Hackathon hackathon = mockVoteHackathon(true);
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(voteRepository.existsByHackathonIdAndVoterId(1L, 1L)).willReturn(false);
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(true); // 내 팀

        assertThatThrownBy(() -> voteService.vote(1L, 1L, voteRequest(1L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.CANNOT_VOTE_OWN_TEAM);
    }

    @Test
    @DisplayName("해당 해커톤에 등록되지 않은 팀에 투표 시 예외")
    void vote_teamNotInHackathon() {
        Hackathon hackathon = mockVoteHackathon(true);
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(voteRepository.existsByHackathonIdAndVoterId(1L, 1L)).willReturn(false);
        given(teamMemberRepository.existsByTeamIdAndUserId(99L, 1L)).willReturn(false);
        given(hackathonRegistrationRepository.findByHackathonIdAndTeamId(1L, 99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> voteService.vote(1L, 1L, voteRequest(99L)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // 리플렉션 헬퍼
    // -------------------------------------------------------------------------

    private void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new RuntimeException("Field not found: " + fieldName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
