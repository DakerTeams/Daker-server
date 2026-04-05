package com.daker.domain.admin;

import com.daker.domain.admin.dto.*;
import com.daker.domain.admin.dto.AdminHackathonCloseResponse;
import com.daker.domain.admin.dto.AdminHackathonUpdateResponse;
import com.daker.domain.admin.service.AdminService;
import com.daker.domain.hackathon.domain.*;
import com.daker.domain.hackathon.repository.*;
import com.daker.domain.judge.repository.JudgeEvaluationRepository;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.domain.xp.service.XpService;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock private HackathonRepository hackathonRepository;
    @Mock private TagRepository tagRepository;
    @Mock private HackathonTagRepository hackathonTagRepository;
    @Mock private HackathonJudgeRepository hackathonJudgeRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private JudgeEvaluationRepository judgeEvaluationRepository;
    @Mock private XpService xpService;

    // -------------------------------------------------------------------------
    // 헬퍼
    // -------------------------------------------------------------------------

    private Hackathon mockHackathon(Long id, HackathonStatus status) {
        Hackathon h = Hackathon.builder()
                .title("해커톤 " + id)
                .summary("요약")
                .description("설명")
                .organizer("주최자")
                .status(status)
                .scoreType(ScoreType.SCORE)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(12))
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(5))
                .maxTeamSize(4)
                .maxParticipants(100)
                .campEnabled(false)
                .allowSolo(false)
                .build();
        setField(h, "id", id);
        setField(h, "createdAt", LocalDateTime.now());
        setField(h, "updatedAt", LocalDateTime.now());
        return h;
    }

    private User mockUser(Long id, Role role) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("유저" + id)
                .password("encoded")
                .role(role)
                .build();
        setField(user, "id", id);
        return user;
    }

    private HackathonJudge mockJudge(Hackathon hackathon, User user) {
        HackathonJudge judge = HackathonJudge.builder()
                .hackathon(hackathon)
                .user(user)
                .build();
        setField(judge, "id", 1L);
        setField(judge, "assignedAt", LocalDateTime.now());
        return judge;
    }

    private HackathonCreateRequest mockCreateRequest() {
        HackathonCreateRequest req = new HackathonCreateRequest();
        setField(req, "title", "새 해커톤");
        setField(req, "organizerName", "주최자");
        setField(req, "scoreType", ScoreType.SCORE);
        setField(req, "startAt", LocalDateTime.now().plusDays(10));
        setField(req, "endAt", LocalDateTime.now().plusDays(12));
        setField(req, "registrationStartAt", LocalDateTime.now().minusDays(1));
        setField(req, "registrationEndAt", LocalDateTime.now().plusDays(5));
        setField(req, "maxTeamSize", 4);
        return req;
    }

    private JudgeRoleRequest mockJudgeRoleRequest(String action) {
        JudgeRoleRequest req = new JudgeRoleRequest();
        setField(req, "action", action);
        return req;
    }

    // -------------------------------------------------------------------------
    // getDashboard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("대시보드 조회 성공")
    void getDashboard_success() {
        List<Hackathon> hackathons = List.of(
                mockHackathon(1L, HackathonStatus.OPEN),
                mockHackathon(2L, HackathonStatus.UPCOMING),
                mockHackathon(3L, HackathonStatus.ENDED)
        );
        Page<Hackathon> hackathonPage = new PageImpl<>(hackathons);
        Page<User> judgePage = new PageImpl<>(List.of(mockUser(1L, Role.JUDGE)));

        given(hackathonRepository.findAllByDeletedFalse()).willReturn(hackathons);
        given(hackathonRepository.findAllByDeletedFalse(any(Pageable.class))).willReturn(hackathonPage);
        given(hackathonRepository.countByCreatedAtAfterAndDeletedFalse(any())).willReturn(1L);
        given(teamRepository.findAllByHackathonId(anyLong())).willReturn(List.of());
        given(teamRepository.count()).willReturn(5L);
        given(teamRepository.countByCreatedAtAfter(any())).willReturn(2L);
        given(userRepository.count()).willReturn(10L);
        given(userRepository.countByCreatedAtAfter(any())).willReturn(3L);
        given(userRepository.findAllByRole(eq(Role.JUDGE), any(Pageable.class))).willReturn(judgePage);
        given(submissionRepository.countByIsLatestTrue()).willReturn(8L);
        given(judgeEvaluationRepository.count()).willReturn(3L);

        AdminDashboardResponse result = adminService.getDashboard(1, 20);

        assertThat(result.getHackathons().getTotal()).isEqualTo(3);
        assertThat(result.getHackathons().getActive()).isEqualTo(1);
        assertThat(result.getHackathons().getUpcoming()).isEqualTo(1);
        assertThat(result.getHackathons().getEnded()).isEqualTo(1);
        assertThat(result.getHackathons().getNewThisMonth()).isEqualTo(1);
        assertThat(result.getParticipatedTeams().getTotal()).isEqualTo(5);
        assertThat(result.getParticipatedTeams().getNewThisWeek()).isEqualTo(2);
        assertThat(result.getUsers().getTotal()).isEqualTo(10);
        assertThat(result.getUsers().getNewThisMonth()).isEqualTo(3);
        assertThat(result.getUsers().getJudges()).isEqualTo(1);
        assertThat(result.getSubmissions().getTotal()).isEqualTo(8);
        assertThat(result.getSubmissions().getPendingReview()).isEqualTo(5);
    }

    // -------------------------------------------------------------------------
    // getHackathons
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("어드민 해커톤 목록 조회 성공")
    void getHackathons_success() {
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        Page<Hackathon> page = new PageImpl<>(List.of(h));
        given(hackathonRepository.findAllByDeletedFalse(any(Pageable.class))).willReturn(page);
        given(teamRepository.findAllByHackathonId(anyLong())).willReturn(List.of());

        var result = adminService.getHackathons(PageRequest.of(0, 20));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getTitle()).isEqualTo("해커톤 1");
        assertThat(result.getItems().get(0).getNumOfTeams()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // createHackathon
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("해커톤 생성 성공")
    void createHackathon_success() {
        HackathonCreateRequest req = mockCreateRequest();

        given(hackathonRepository.save(any(Hackathon.class))).willAnswer(inv -> {
            Hackathon h = inv.getArgument(0);
            setField(h, "id", 1L);
            setField(h, "createdAt", LocalDateTime.now());
            setField(h, "updatedAt", LocalDateTime.now());
            return h;
        });

        AdminHackathonCreateResponse result = adminService.createHackathon(req);

        assertThat(result.getTitle()).isEqualTo("새 해커톤");
        assertThat(result.getStatus()).isEqualTo(HackathonStatus.UPCOMING);
        verify(hackathonRepository).save(any(Hackathon.class));
    }

    // -------------------------------------------------------------------------
    // updateHackathon
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("해커톤 수정 성공")
    void updateHackathon_success() {
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        HackathonUpdateRequest req = new HackathonUpdateRequest();
        setField(req, "title", "수정된 제목");
        setField(req, "status", HackathonStatus.CLOSED);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));

        AdminHackathonUpdateResponse result = adminService.updateHackathon(1L, req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(HackathonStatus.CLOSED);
    }

    @Test
    @DisplayName("존재하지 않는 해커톤 수정 시 예외")
    void updateHackathon_notFound() {
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateHackathon(99L, new HackathonUpdateRequest()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // closeHackathon
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("해커톤 마감 성공")
    void closeHackathon_success() {
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));

        AdminHackathonCloseResponse result = adminService.closeHackathon(1L);

        assertThat(result.getStatus()).isEqualTo(HackathonStatus.CLOSED);
    }

    @Test
    @DisplayName("존재하지 않는 해커톤 마감 시 예외")
    void closeHackathon_notFound() {
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.closeHackathon(99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    @Test
    @DisplayName("해커톤 소프트 삭제 성공")
    void deleteHackathon_success() {
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));

        adminService.deleteHackathon(1L);

        assertThat(h.isDeleted()).isTrue();
    }

    // -------------------------------------------------------------------------
    // getUsers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("전체 유저 목록 조회 성공")
    void getUsers_noFilter() {
        Page<User> page = new PageImpl<>(List.of(mockUser(1L, Role.USER), mockUser(2L, Role.JUDGE)));
        given(userRepository.findAll(any(Pageable.class))).willReturn(page);
        given(teamRepository.findAllByUserId(anyLong())).willReturn(List.of());

        var result = adminService.getUsers(null, PageRequest.of(0, 20));

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getJoinedHackathons()).isEqualTo(0);
    }

    @Test
    @DisplayName("역할 필터로 유저 목록 조회 성공")
    void getUsers_withRoleFilter() {
        Page<User> page = new PageImpl<>(List.of(mockUser(1L, Role.JUDGE)));
        given(userRepository.findAllByRole(eq(Role.JUDGE), any(Pageable.class))).willReturn(page);
        given(teamRepository.findAllByUserId(anyLong())).willReturn(List.of());

        var result = adminService.getUsers(Role.JUDGE, PageRequest.of(0, 20));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getRole()).isEqualTo(Role.JUDGE);
        assertThat(result.getItems().get(0).getJoinedHackathons()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // getJudges
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("심사위원 목록 조회 성공")
    void getJudges_success() {
        User judge = mockUser(1L, Role.JUDGE);
        Page<User> page = new PageImpl<>(List.of(judge));
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        HackathonJudge assignment = mockJudge(h, judge);

        given(userRepository.findAllByRole(eq(Role.JUDGE), any(Pageable.class))).willReturn(page);
        given(hackathonJudgeRepository.findAllByUserId(1L)).willReturn(List.of(assignment));

        var result = adminService.getJudges(null, PageRequest.of(0, 20));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getAssignedHackathons()).containsExactly("해커톤 1");
    }

    // -------------------------------------------------------------------------
    // updateJudgeRole
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("심사위원 권한 부여 성공")
    void updateJudgeRole_grant_success() {
        User user = mockUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        AdminUserResponse result = adminService.updateJudgeRole(1L, mockJudgeRoleRequest("grant"));

        assertThat(result.getRole()).isEqualTo(Role.JUDGE);
    }

    @Test
    @DisplayName("이미 심사위원인 유저에게 권한 부여 시 예외")
    void updateJudgeRole_grant_alreadyJudge() {
        User user = mockUser(1L, Role.JUDGE);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.updateJudgeRole(1L, mockJudgeRoleRequest("grant")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_A_JUDGE);
    }

    @Test
    @DisplayName("심사위원 권한 회수 성공")
    void updateJudgeRole_revoke_success() {
        User user = mockUser(1L, Role.JUDGE);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        AdminUserResponse result = adminService.updateJudgeRole(1L, mockJudgeRoleRequest("revoke"));

        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("심사위원이 아닌 유저의 권한 회수 시 예외")
    void updateJudgeRole_revoke_notJudge() {
        User user = mockUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.updateJudgeRole(1L, mockJudgeRoleRequest("revoke")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_A_JUDGE);
    }

    @Test
    @DisplayName("잘못된 action 값으로 요청 시 예외")
    void updateJudgeRole_invalidAction() {
        User user = mockUser(1L, Role.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.updateJudgeRole(1L, mockJudgeRoleRequest("unknown")))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    // -------------------------------------------------------------------------
    // assignJudge
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("심사위원 배정 성공")
    void assignJudge_success() {
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        User judge = mockUser(1L, Role.JUDGE);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(userRepository.findById(1L)).willReturn(Optional.of(judge));
        given(hackathonJudgeRepository.existsByHackathonIdAndUserId(1L, 1L)).willReturn(false);

        adminService.assignJudge(1L, 1L);

        verify(hackathonJudgeRepository).save(any(HackathonJudge.class));
    }

    @Test
    @DisplayName("심사위원이 아닌 유저 배정 시 예외")
    void assignJudge_notJudge() {
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        User user = mockUser(1L, Role.USER);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.assignJudge(1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_A_JUDGE);
    }

    @Test
    @DisplayName("이미 배정된 심사위원 중복 배정 시 예외")
    void assignJudge_alreadyAssigned() {
        Hackathon h = mockHackathon(1L, HackathonStatus.OPEN);
        User judge = mockUser(1L, Role.JUDGE);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(h));
        given(userRepository.findById(1L)).willReturn(Optional.of(judge));
        given(hackathonJudgeRepository.existsByHackathonIdAndUserId(1L, 1L)).willReturn(true);

        assertThatThrownBy(() -> adminService.assignJudge(1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.JUDGE_ALREADY_ASSIGNED);
    }

    // -------------------------------------------------------------------------
    // removeJudge
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("심사위원 배정 해제 성공")
    void removeJudge_success() {
        given(hackathonJudgeRepository.existsByHackathonIdAndUserId(1L, 1L)).willReturn(true);

        adminService.removeJudge(1L, 1L);

        verify(hackathonJudgeRepository).deleteByHackathonIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("배정되지 않은 심사위원 해제 시 예외")
    void removeJudge_notAssigned() {
        given(hackathonJudgeRepository.existsByHackathonIdAndUserId(1L, 99L)).willReturn(false);

        assertThatThrownBy(() -> adminService.removeJudge(1L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.JUDGE_NOT_ASSIGNED);
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
