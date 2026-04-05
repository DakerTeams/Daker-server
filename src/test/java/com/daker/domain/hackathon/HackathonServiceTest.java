package com.daker.domain.hackathon;

import com.daker.domain.hackathon.domain.*;
import com.daker.domain.hackathon.dto.*;
import com.daker.domain.hackathon.repository.*;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.domain.TeamMember;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.hackathon.service.HackathonService;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.judge.repository.JudgeEvaluationRepository;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.vote.repository.VoteRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class HackathonServiceTest {

    @InjectMocks
    private HackathonService hackathonService;

    @Mock private HackathonRepository hackathonRepository;
    @Mock private HackathonTagRepository hackathonTagRepository;
    @Mock private HackathonRegistrationRepository registrationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private JudgeEvaluationRepository judgeEvaluationRepository;

    // -------------------------------------------------------------------------
    // 헬퍼
    // -------------------------------------------------------------------------

    private Hackathon mockOpenHackathon() {
        Hackathon h = Hackathon.builder()
                .title("Test Hackathon")
                .description("desc")
                .organizer("org")
                .status(HackathonStatus.OPEN)
                .scoreType(ScoreType.SCORE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(3))
                .maxTeamSize(5)
                .build();
        setField(h, "id", 1L);
        return h;
    }

    private Hackathon mockEndedHackathon() {
        Hackathon h = Hackathon.builder()
                .title("Ended Hackathon")
                .description("desc")
                .organizer("org")
                .status(HackathonStatus.ENDED)
                .scoreType(ScoreType.SCORE)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().minusDays(1))
                .registrationStartDate(LocalDateTime.now().minusDays(14))
                .registrationEndDate(LocalDateTime.now().minusDays(8))
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

    // -------------------------------------------------------------------------
    // getHackathons
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("해커톤 목록 조회 성공")
    void getHackathons_success() {
        Hackathon h = mockOpenHackathon();
        Page<Hackathon> page = new PageImpl<>(List.of(h));

        given(hackathonRepository.findAllWithFilters(any(), anyBoolean(), any(), any(), any())).willReturn(page);
        given(hackathonTagRepository.findAllByHackathonId(h.getId())).willReturn(List.of());
        given(teamRepository.findAllByHackathonId(h.getId())).willReturn(List.of());

        var result = hackathonService.getHackathons(null, null, null, PageRequest.of(0, 20));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getTitle()).isEqualTo("Test Hackathon");
    }

    @Test
    @DisplayName("해커톤 목록 조회 - 참가자 수 합산 포함")
    void getHackathons_participantCount() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        TeamMember member = TeamMember.builder().team(team).user(leader).build();
        team.getMembers().add(member);

        Page<Hackathon> page = new PageImpl<>(List.of(h));
        given(hackathonRepository.findAllWithFilters(any(), anyBoolean(), any(), any(), any())).willReturn(page);
        given(hackathonTagRepository.findAllByHackathonId(h.getId())).willReturn(List.of());
        given(teamRepository.findAllByHackathonId(h.getId())).willReturn(List.of(team));

        var result = hackathonService.getHackathons(null, null, null, PageRequest.of(0, 20));

        assertThat(result.getItems().get(0).getParticipants()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // getHackathon
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("해커톤 단건 조회 성공")
    void getHackathon_success() {
        Hackathon h = mockOpenHackathon();
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(hackathonTagRepository.findAllByHackathonId(1L)).willReturn(List.of());
        given(teamRepository.findAllByHackathonId(1L)).willReturn(List.of());

        HackathonDetailResponse response = hackathonService.getHackathon(1L);

        assertThat(response.getTitle()).isEqualTo("Test Hackathon");
    }

    @Test
    @DisplayName("존재하지 않는 해커톤 조회 시 예외")
    void getHackathon_notFound() {
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> hackathonService.getHackathon(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // getRegistrationStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("등록 상태 조회 성공")
    void getRegistrationStatus_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);
        HackathonRegistration reg = mockRegistration(h, team);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByUserId(1L)).willReturn(List.of(team));
        given(registrationRepository.findByTeamId(1L)).willReturn(Optional.of(reg));

        RegistrationStatusResponse response = hackathonService.getRegistrationStatus(1L, 1L);

        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getTeamName()).isEqualTo("Team 1");
    }

    @Test
    @DisplayName("해당 해커톤에 팀이 없으면 미신청 상태 반환")
    void getRegistrationStatus_notRegistered() {
        Hackathon h = mockOpenHackathon();
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByUserId(1L)).willReturn(List.of());

        RegistrationStatusResponse response = hackathonService.getRegistrationStatus(1L, 1L);

        assertThat(response.isRegistered()).isFalse();
        assertThat(response.getRegistrationId()).isNull();
        assertThat(response.getTeamId()).isNull();
    }

    // -------------------------------------------------------------------------
    // cancelRegistration
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("참가 신청 취소 성공")
    void cancelRegistration_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);
        HackathonRegistration reg = mockRegistration(h, team);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByUserId(1L)).willReturn(List.of(team));
        given(registrationRepository.findByTeamId(1L)).willReturn(Optional.of(reg));

        hackathonService.cancelRegistration(1L, 1L);

        verify(registrationRepository).delete(reg);
    }

    @Test
    @DisplayName("접수 기간이 아니면 취소 불가")
    void cancelRegistration_periodInvalid() {
        Hackathon h = mockEndedHackathon();
        given(hackathonRepository.findByIdAndDeletedFalse(2L)).willReturn(Optional.of(h));

        assertThatThrownBy(() -> hackathonService.cancelRegistration(2L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.REGISTRATION_PERIOD_INVALID);
    }

    @Test
    @DisplayName("팀장이 아니면 취소 불가")
    void cancelRegistration_notLeader() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByUserId(2L)).willReturn(List.of(team));

        assertThatThrownBy(() -> hackathonService.cancelRegistration(1L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_TEAM_LEADER);
    }

    // -------------------------------------------------------------------------
    // getLeaderboard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("리더보드 조회 - 미제출 팀 포함")
    void getLeaderboard_notSubmitted() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByHackathonId(1L)).willReturn(List.of(team));
        given(submissionRepository.findByTeamIdAndHackathonIdAndIsLatestTrue(anyLong(), anyLong()))
                .willReturn(Optional.empty());

        LeaderboardResponse response = hackathonService.getLeaderboard(1L);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).isSubmitted()).isFalse();
        assertThat(response.getItems().get(0).getScore()).isNull();
    }

    @Test
    @DisplayName("리더보드 조회 - scoreType 포함")
    void getLeaderboard_scoreType() {
        Hackathon h = mockOpenHackathon();
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByHackathonId(1L)).willReturn(List.of());

        LeaderboardResponse response = hackathonService.getLeaderboard(1L);

        assertThat(response.getScoreType()).isEqualTo("SCORE");
    }

    @Test
    @DisplayName("존재하지 않는 해커톤 리더보드 조회 시 예외")
    void getLeaderboard_notFound() {
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> hackathonService.getLeaderboard(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    @Test
    @DisplayName("VOTE 방식 리더보드 - 해커톤 종료 전에는 rank/score 모두 null")
    void getLeaderboard_vote_beforeEnd() {
        Hackathon h = Hackathon.builder()
                .title("Vote Hackathon")
                .organizer("org")
                .status(HackathonStatus.OPEN)
                .scoreType(ScoreType.VOTE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(3))
                .registrationStartDate(LocalDateTime.now().minusDays(5))
                .registrationEndDate(LocalDateTime.now().minusDays(1))
                .maxTeamSize(5)
                .build();
        setField(h, "id", 10L);

        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(hackathonRepository.findByIdAndDeletedFalse(10L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByHackathonId(10L)).willReturn(List.of(team));

        LeaderboardResponse response = hackathonService.getLeaderboard(10L);

        assertThat(response.getScoreType()).isEqualTo("VOTE");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRank()).isNull();
        assertThat(response.getItems().get(0).getScore()).isNull();
    }

    @Test
    @DisplayName("VOTE 방식 리더보드 - 해커톤 종료 후 득표수 기준 rank 반환, score는 null")
    void getLeaderboard_vote_afterEnd() {
        Hackathon h = Hackathon.builder()
                .title("Vote Hackathon Ended")
                .organizer("org")
                .status(HackathonStatus.ENDED)
                .scoreType(ScoreType.VOTE)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().minusDays(1))
                .registrationStartDate(LocalDateTime.now().minusDays(14))
                .registrationEndDate(LocalDateTime.now().minusDays(8))
                .maxTeamSize(5)
                .build();
        setField(h, "id", 11L);

        User leader = mockUser(1L);
        Team team1 = mockTeam(1L, h, leader);
        Team team2 = mockTeam(2L, h, leader);

        // team1: 5표, team2: 3표
        given(hackathonRepository.findByIdAndDeletedFalse(11L)).willReturn(Optional.of(h));
        given(teamRepository.findAllByHackathonId(11L)).willReturn(List.of(team1, team2));
        given(voteRepository.countByHackathonIdGroupByTeam(11L))
                .willReturn(List.of(new Object[]{1L, 5L}, new Object[]{2L, 3L}));

        LeaderboardResponse response = hackathonService.getLeaderboard(11L);

        assertThat(response.getScoreType()).isEqualTo("VOTE");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getRank()).isEqualTo(1);
        assertThat(response.getItems().get(0).getTeamName()).isEqualTo("Team 1");
        assertThat(response.getItems().get(1).getRank()).isEqualTo(2);
        assertThat(response.getItems().get(0).getScore()).isNull();
    }

    // -------------------------------------------------------------------------
    // 리플렉션 헬퍼
    // -------------------------------------------------------------------------

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
