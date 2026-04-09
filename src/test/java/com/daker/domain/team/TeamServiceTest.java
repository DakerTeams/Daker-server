package com.daker.domain.team;

import com.daker.domain.hackathon.domain.*;
import com.daker.domain.hackathon.repository.HackathonRegistrationRepository;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.team.domain.*;
import com.daker.domain.team.dto.*;
import com.daker.domain.team.repository.*;
import com.daker.domain.team.service.TeamService;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private TeamApplicationRepository teamApplicationRepository;
    @Mock private HackathonRepository hackathonRepository;
    @Mock private HackathonRegistrationRepository registrationRepository;
    @Mock private UserRepository userRepository;
    @Mock private com.daker.domain.team.repository.TeamPrivateInfoRepository teamPrivateInfoRepository;

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
                .maxTeamSize(3)
                .build();
        setField(h, "id", 1L);
        return h;
    }

    private Hackathon mockClosedHackathon() {
        Hackathon h = Hackathon.builder()
                .title("Closed Hackathon")
                .description("desc")
                .organizer("org")
                .status(HackathonStatus.ENDED)
                .scoreType(ScoreType.SCORE)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().minusDays(1))
                .registrationStartDate(LocalDateTime.now().minusDays(14))
                .registrationEndDate(LocalDateTime.now().minusDays(8))
                .maxTeamSize(3)
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

    private TeamCreateRequest mockCreateRequest(Long hackathonId) {
        TeamCreateRequest req = new TeamCreateRequest();
        setField(req, "hackathonId", hackathonId);
        setField(req, "name", "New Team");
        setField(req, "description", "desc");
        setField(req, "isOpen", true);
        return req;
    }

    private TeamUpdateRequest mockUpdateRequest() {
        TeamUpdateRequest req = new TeamUpdateRequest();
        setField(req, "name", "Updated Name");
        setField(req, "description", "updated desc");
        setField(req, "isOpen", false);
        return req;
    }

    // -------------------------------------------------------------------------
    // getTeams
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("팀 목록 조회 성공")
    void getTeams_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findAllWithFilters(any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(team)));

        var result = teamService.getTeams(1L, null, null, PageRequest.of(0, 20));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Team 1");
        assertThat(result.getItems().get(0).getHackathonTitle()).isEqualTo("Test Hackathon");
    }

    @Test
    @DisplayName("팀 목록 조회 기본 정렬은 최신 등록순이다")
    void getTeams_defaultSortByCreatedAtDesc() {
        given(teamRepository.findAllWithFilters(any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

        teamService.getTeams(1L, true, "test", PageRequest.of(0, 20));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(teamRepository).findAllWithFilters(eq(1L), eq(true), eq("test"), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        assertThat(pageable.getSort().getOrderFor("id")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("id").isDescending()).isTrue();
    }

    // -------------------------------------------------------------------------
    // getMyTeams
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("내 팀 목록 조회 성공")
    void getMyTeams_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findAllByUserId(1L)).willReturn(List.of(team));

        List<TeamSummaryResponse> result = teamService.getMyTeams(1L, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Team 1");
        assertThat(result.get(0).getHackathonTitle()).isEqualTo("Test Hackathon");
    }

    // -------------------------------------------------------------------------
    // createTeam
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("팀 생성 성공")
    void createTeam_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);
        TeamCreateRequest req = mockCreateRequest(1L);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamMemberRepository.existsByUserIdAndHackathonId(1L, 1L)).willReturn(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(leader));
        given(teamRepository.save(any())).willReturn(team);

        TeamDetailResponse response = teamService.createTeam(req, 1L);

        assertThat(response.getName()).isEqualTo("New Team");
        verify(teamMemberRepository).save(any(TeamMember.class));
        verify(registrationRepository).save(any(HackathonRegistration.class));
    }

    @Test
    @DisplayName("존재하지 않는 해커톤으로 팀 생성 시 예외")
    void createTeam_hackathonNotFound() {
        TeamCreateRequest req = mockCreateRequest(99L);
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam(req, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    @Test
    @DisplayName("접수 기간이 아닌 해커톤에서 팀 생성 시 예외")
    void createTeam_registrationClosed() {
        Hackathon h = mockClosedHackathon();
        TeamCreateRequest req = mockCreateRequest(2L);

        given(hackathonRepository.findByIdAndDeletedFalse(2L)).willReturn(Optional.of(h));

        assertThatThrownBy(() -> teamService.createTeam(req, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.REGISTRATION_PERIOD_INVALID);
    }

    @Test
    @DisplayName("이미 해당 해커톤에 팀이 있으면 생성 불가")
    void createTeam_alreadyInTeam() {
        Hackathon h = mockOpenHackathon();
        TeamCreateRequest req = mockCreateRequest(1L);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(teamMemberRepository.existsByUserIdAndHackathonId(1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> teamService.createTeam(req, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_ALREADY_EXISTS);
    }

    // -------------------------------------------------------------------------
    // updateTeam
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("팀 정보 수정 성공")
    void updateTeam_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);
        TeamUpdateRequest req = mockUpdateRequest();

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamPrivateInfoRepository.findByTeamId(1L)).willReturn(Optional.empty());

        TeamDetailResponse response = teamService.updateTeam(1L, req, 1L);

        assertThat(response.getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("팀 수정 시 연락 수단 저장")
    void updateTeam_saveContact() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);
        TeamUpdateRequest req = mockUpdateRequest();
        setField(req, "contactType", "KAKAO");
        setField(req, "contactValue", "https://open.kakao.com/test");

        com.daker.domain.team.domain.TeamPrivateInfo privateInfo =
                com.daker.domain.team.domain.TeamPrivateInfo.builder()
                        .team(team).contactType("KAKAO").contactValue("https://open.kakao.com/test").build();

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamPrivateInfoRepository.findByTeamId(1L))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(privateInfo));
        given(teamPrivateInfoRepository.save(any())).willReturn(privateInfo);

        TeamDetailResponse response = teamService.updateTeam(1L, req, 1L);

        verify(teamPrivateInfoRepository, atLeastOnce()).save(any());
        assertThat(response.getContact()).isNotNull();
        assertThat(response.getContact().getType()).isEqualTo("KAKAO");
    }

    @Test
    @DisplayName("팀 상세 조회 - 팀원이면 contact 포함")
    void getTeam_memberSeesContact() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        com.daker.domain.team.domain.TeamPrivateInfo privateInfo =
                com.daker.domain.team.domain.TeamPrivateInfo.builder()
                        .team(team).contactType("KAKAO").contactValue("https://open.kakao.com/test").build();

        given(teamRepository.findByIdWithDetails(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(true);
        given(teamPrivateInfoRepository.findByTeamId(1L)).willReturn(Optional.of(privateInfo));

        TeamDetailResponse response = teamService.getTeam(1L, 1L);

        assertThat(response.getContact()).isNotNull();
        assertThat(response.getContact().getValue()).isEqualTo("https://open.kakao.com/test");
    }

    @Test
    @DisplayName("팀 상세 조회 - 비팀원이면 contact null")
    void getTeam_nonMemberNoContact() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findByIdWithDetails(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 99L)).willReturn(false);

        TeamDetailResponse response = teamService.getTeam(1L, 99L);

        assertThat(response.getContact()).isNull();
    }

    @Test
    @DisplayName("팀장이 아닌 경우 팀 수정 불가")
    void updateTeam_notLeader() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);
        TeamUpdateRequest req = mockUpdateRequest();

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.updateTeam(1L, req, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_TEAM_LEADER);
    }

    // -------------------------------------------------------------------------
    // deleteTeam
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("팀 삭제 성공")
    void deleteTeam_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(registrationRepository.findByTeamId(1L)).willReturn(Optional.empty());

        teamService.deleteTeam(1L, 1L);

        verify(teamApplicationRepository).deleteAllByTeamId(1L);
    }

    @Test
    @DisplayName("접수 기간이 아닌 경우 팀 삭제 불가")
    void deleteTeam_registrationClosed() {
        Hackathon h = mockClosedHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.deleteTeam(1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_APPLICATION_CLOSED);
    }

    // -------------------------------------------------------------------------
    // apply
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("팀 참가 신청 성공")
    void apply_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);
        User applicant = mockUser(2L);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamApplicationRepository.existsByTeamIdAndUserIdAndStatus(1L, 2L, ApplicationStatus.PENDING)).willReturn(false);
        given(teamMemberRepository.existsByUserIdAndHackathonId(2L, 1L)).willReturn(false);
        given(userRepository.findById(2L)).willReturn(Optional.of(applicant));

        teamService.apply(1L, 2L, null);

        verify(teamApplicationRepository).save(any(TeamApplication.class));
    }

    @Test
    @DisplayName("접수 기간이 아닌 경우 신청 불가")
    void apply_closed() {
        Hackathon h = mockClosedHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.apply(1L, 2L, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_APPLICATION_CLOSED);
    }

    @Test
    @DisplayName("팀이 가득 찬 경우 신청 불가")
    void apply_teamFull() {
        Hackathon h = mockOpenHackathon(); // maxTeamSize = 3
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        // maxMemberCount=3, currentMemberCount=3 → isFull = true
        setField(team, "maxMemberCount", 3);
        setField(team, "currentMemberCount", 3);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.apply(1L, 2L, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_FULL);
    }

    @Test
    @DisplayName("이미 신청한 경우 재신청 불가")
    void apply_alreadyApplied() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamApplicationRepository.existsByTeamIdAndUserIdAndStatus(1L, 2L, ApplicationStatus.PENDING)).willReturn(true);

        assertThatThrownBy(() -> teamService.apply(1L, 2L, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_APPLIED);
    }

    @Test
    @DisplayName("이미 해당 해커톤에 팀이 있으면 신청 불가")
    void apply_alreadyInTeam() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamApplicationRepository.existsByTeamIdAndUserIdAndStatus(1L, 2L, ApplicationStatus.PENDING)).willReturn(false);
        given(teamMemberRepository.existsByUserIdAndHackathonId(2L, 1L)).willReturn(true);

        assertThatThrownBy(() -> teamService.apply(1L, 2L, null))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_ALREADY_EXISTS);
    }

    // -------------------------------------------------------------------------
    // getApplications
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("팀장은 신청 목록 조회 가능")
    void getApplications_success() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamApplicationRepository.findAllByTeamIdAndStatus(1L, ApplicationStatus.PENDING))
                .willReturn(List.of());

        List<TeamApplicationResponse> result = teamService.getApplications(1L, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팀장이 아니면 신청 목록 조회 불가")
    void getApplications_notLeader() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.getApplications(1L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_TEAM_LEADER);
    }

    // -------------------------------------------------------------------------
    // decideApplication
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("신청 수락 성공 - 팀원 자동 추가")
    void decideApplication_accept() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        User applicant = mockUser(2L);
        Team team = mockTeam(1L, h, leader);

        TeamApplication application = TeamApplication.builder().team(team).user(applicant).build();
        setField(application, "id", 10L);

        ApplicationDecisionRequest req = new ApplicationDecisionRequest();
        setField(req, "status", ApplicationStatus.ACCEPTED);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamApplicationRepository.findById(10L)).willReturn(Optional.of(application));
        given(userRepository.findById(1L)).willReturn(Optional.of(leader));

        TeamApplicationResponse response = teamService.decideApplication(1L, 10L, req, 1L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("신청 거절 성공")
    void decideApplication_reject() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        User applicant = mockUser(2L);
        Team team = mockTeam(1L, h, leader);

        TeamApplication application = TeamApplication.builder().team(team).user(applicant).build();
        setField(application, "id", 10L);

        ApplicationDecisionRequest req = new ApplicationDecisionRequest();
        setField(req, "status", ApplicationStatus.REJECTED);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamApplicationRepository.findById(10L)).willReturn(Optional.of(application));
        given(userRepository.findById(1L)).willReturn(Optional.of(leader));

        TeamApplicationResponse response = teamService.decideApplication(1L, 10L, req, 1L);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(teamMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("팀장이 아닌 경우 신청 처리 불가")
    void decideApplication_notLeader() {
        Hackathon h = mockOpenHackathon();
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        ApplicationDecisionRequest req = new ApplicationDecisionRequest();
        setField(req, "status", ApplicationStatus.ACCEPTED);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.decideApplication(1L, 10L, req, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_TEAM_LEADER);
    }

    @Test
    @DisplayName("팀이 가득 찬 경우 수락 불가")
    void decideApplication_teamFull() {
        Hackathon h = mockOpenHackathon(); // maxTeamSize = 3
        User leader = mockUser(1L);
        Team team = mockTeam(1L, h, leader);

        // maxMemberCount=3, currentMemberCount=3 → isFull = true
        setField(team, "maxMemberCount", 3);
        setField(team, "currentMemberCount", 3);

        ApplicationDecisionRequest req = new ApplicationDecisionRequest();
        setField(req, "status", ApplicationStatus.ACCEPTED);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.decideApplication(1L, 10L, req, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_FULL);
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
            throw new NoSuchFieldException(fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
