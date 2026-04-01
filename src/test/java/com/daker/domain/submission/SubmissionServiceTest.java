package com.daker.domain.submission;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import com.daker.domain.hackathon.domain.ScoreType;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.judge.repository.JudgeEvaluationRepository;
import com.daker.domain.submission.domain.Submission;
import com.daker.domain.submission.domain.SubmissionItem;
import com.daker.domain.submission.domain.SubmissionStatus;
import com.daker.domain.submission.dto.AdminSubmissionResponse;
import com.daker.domain.submission.dto.SubmissionCreateResponse;
import com.daker.domain.submission.dto.SubmissionHistoryResponse;
import com.daker.domain.submission.dto.SubmissionStatusResponse;
import com.daker.domain.submission.repository.SubmissionItemRepository;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.submission.service.SubmissionService;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.daker.global.infra.S3Uploader;
import com.daker.global.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @InjectMocks
    private SubmissionService submissionService;

    @Mock private SubmissionRepository submissionRepository;
    @Mock private SubmissionItemRepository submissionItemRepository;
    @Mock private HackathonRepository hackathonRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private JudgeEvaluationRepository judgeEvaluationRepository;
    @Mock private S3Uploader s3Uploader;

    // -------------------------------------------------------------------------
    // 헬퍼
    // -------------------------------------------------------------------------

    private Hackathon mockHackathon(LocalDateTime deadline) {
        Hackathon h = Hackathon.builder()
                .title("Test Hackathon")
                .organizer("org")
                .status(HackathonStatus.OPEN)
                .scoreType(ScoreType.SCORE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(7))
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(3))
                .submissionDeadlineAt(deadline)
                .maxTeamSize(5)
                .build();
        setField(h, "id", 1L);
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

    private Submission mockSubmission(Hackathon hackathon, Team team, User submitter) {
        return mockSubmission(hackathon, team, submitter, 1, true);
    }

    private Submission mockSubmission(Hackathon hackathon, Team team, User submitter, int revisionNo, boolean isLatest) {
        Submission submission = Submission.builder()
                .hackathon(hackathon)
                .team(team)
                .submitter(submitter)
                .status(SubmissionStatus.SUBMITTED)
                .revisionNo(revisionNo)
                .isLatest(isLatest)
                .submittedAt(LocalDateTime.now())
                .build();
        setField(submission, "id", (long) revisionNo);
        return submission;
    }

    private MockMultipartFile mockFile() {
        return new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
    }

    // -------------------------------------------------------------------------
    // submit
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("최초 제출 성공 - revision 1 생성")
    void submit_first_success() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission submission = mockSubmission(hackathon, team, user, 1, true);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(true);
        given(submissionRepository.findByTeamIdAndHackathonIdAndIsLatestTrue(1L, 1L)).willReturn(Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(submissionRepository.save(any())).willReturn(submission);
        given(s3Uploader.upload(any(), any())).willReturn("daker/submissions/hackathon-1/team-1/uuid_test.pdf");

        SubmissionCreateResponse response = submissionService.submit(1L, 1L, mockFile(), "팀 소개", 1L);

        assertThat(response.getSubmissionId()).isEqualTo(1L);
        assertThat(response.getRevision()).isEqualTo(1);

        ArgumentCaptor<SubmissionItem> captor = ArgumentCaptor.forClass(SubmissionItem.class);
        verify(submissionItemRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).anyMatch(i -> "팀 소개".equals(i.getValueText()));
        assertThat(captor.getAllValues()).anyMatch(i -> i.getFileName() != null);
        verify(s3Uploader).upload(any(), any());
    }

    @Test
    @DisplayName("재제출 성공 - 기존 비활성화 후 revision 증가")
    void submit_resubmit_incrementsRevision() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission existing = mockSubmission(hackathon, team, user, 2, true);
        Submission newSubmission = mockSubmission(hackathon, team, user, 3, true);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(true);
        given(submissionRepository.findByTeamIdAndHackathonIdAndIsLatestTrue(1L, 1L)).willReturn(Optional.of(existing));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(submissionRepository.save(any())).willReturn(newSubmission);

        SubmissionCreateResponse response = submissionService.submit(1L, 1L, null, "재제출", 1L);

        assertThat(response.getRevision()).isEqualTo(3);
        verify(submissionRepository).deactivateAllByTeamIdAndHackathonId(1L, 1L);
    }

    @Test
    @DisplayName("파일 없이 memo만 제출 - S3 업로드 생략")
    void submit_memoOnly_skipsS3() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission submission = mockSubmission(hackathon, team, user);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(true);
        given(submissionRepository.findByTeamIdAndHackathonIdAndIsLatestTrue(1L, 1L)).willReturn(Optional.empty());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(submissionRepository.save(any())).willReturn(submission);

        submissionService.submit(1L, 1L, null, "memo", 1L);

        verify(s3Uploader, never()).upload(any(), any());
    }

    @Test
    @DisplayName("해커톤 없으면 예외")
    void submit_hackathonNotFound() {
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submit(99L, 1L, null, "memo", 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    @Test
    @DisplayName("제출 마감 초과 시 예외")
    void submit_deadlineExceeded() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().minusHours(1));
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));

        assertThatThrownBy(() -> submissionService.submit(1L, 1L, null, "memo", 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SUBMISSION_DEADLINE_EXCEEDED);
    }

    @Test
    @DisplayName("팀 없으면 예외")
    void submit_teamNotFound() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submit(1L, 1L, null, "memo", 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_NOT_FOUND);
    }

    @Test
    @DisplayName("팀원이 아니면 예외")
    void submit_notTeamMember() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User leader = mockUser(1L);
        Team team = mockTeam(1L, hackathon, leader);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 2L)).willReturn(false);

        assertThatThrownBy(() -> submissionService.submit(1L, 2L, null, "memo", 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // getMySubmissionStatus
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("제출한 경우 - submitted: true, canResubmit: true")
    void getMySubmissionStatus_submitted() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission submission = mockSubmission(hackathon, team, user, 3, true);

        SubmissionItem memoItem = SubmissionItem.builder()
                .submission(submission).valueText("팀 소개").isFinal(true).build();

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findMyLatestSubmission(1L, 1L)).willReturn(Optional.of(submission));
        given(submissionItemRepository.findAllBySubmissionId(3L)).willReturn(List.of(memoItem));

        SubmissionStatusResponse response = submissionService.getMySubmissionStatus(1L, 1L);

        assertThat(response.isSubmitted()).isTrue();
        assertThat(response.getRevision()).isEqualTo(3);
        assertThat(response.isCanResubmit()).isTrue();
        assertThat(response.getMemo()).isEqualTo("팀 소개");
    }

    @Test
    @DisplayName("미제출인 경우 - submitted: false, canResubmit: true")
    void getMySubmissionStatus_notSubmitted() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findMyLatestSubmission(1L, 1L)).willReturn(Optional.empty());

        SubmissionStatusResponse response = submissionService.getMySubmissionStatus(1L, 1L);

        assertThat(response.isSubmitted()).isFalse();
        assertThat(response.getRevision()).isEqualTo(0);
        assertThat(response.isCanResubmit()).isTrue();
    }

    @Test
    @DisplayName("해커톤 없으면 예외")
    void getMySubmissionStatus_hackathonNotFound() {
        given(hackathonRepository.findByIdAndDeletedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.getMySubmissionStatus(99L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.HACKATHON_NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // getMySubmissionHistory
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("이력 조회 성공 - 최신순 반환")
    void getMySubmissionHistory_success() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission v3 = mockSubmission(hackathon, team, user, 3, true);
        Submission v2 = mockSubmission(hackathon, team, user, 2, false);
        Submission v1 = mockSubmission(hackathon, team, user, 1, false);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findAllMySubmissions(1L, 1L)).willReturn(List.of(v3, v2, v1));
        given(submissionItemRepository.findAllBySubmissionId(any())).willReturn(List.of());

        List<SubmissionHistoryResponse> result = submissionService.getMySubmissionHistory(1L, 1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRevisionNo()).isEqualTo(3);
        assertThat(result.get(0).isLatest()).isTrue();
        assertThat(result.get(1).getRevisionNo()).isEqualTo(2);
        assertThat(result.get(2).getRevisionNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("이력 없으면 빈 리스트 반환")
    void getMySubmissionHistory_empty() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findAllMySubmissions(1L, 1L)).willReturn(List.of());

        List<SubmissionHistoryResponse> result = submissionService.getMySubmissionHistory(1L, 1L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // activateVersion
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("버전 활성화 성공 - 전체 비활성 후 지정 버전 활성화")
    void activateVersion_success() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission v2 = mockSubmission(hackathon, team, user, 2, false);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findById(2L)).willReturn(Optional.of(v2));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(true);
        given(submissionItemRepository.findAllBySubmissionId(2L)).willReturn(List.of());

        SubmissionStatusResponse response = submissionService.activateVersion(1L, 2L, 1L);

        verify(submissionRepository).deactivateAllByTeamIdAndHackathonId(1L, 1L);
        assertThat(response.getRevision()).isEqualTo(2);
        assertThat(response.isSubmitted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 제출물 활성화 시 예외")
    void activateVersion_notFound() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.activateVersion(1L, 99L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SUBMISSION_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 해커톤의 제출물 활성화 시 예외")
    void activateVersion_wrongHackathon() {
        Hackathon hackathon1 = mockHackathon(LocalDateTime.now().plusDays(1));
        Hackathon hackathon2 = mockHackathon(LocalDateTime.now().plusDays(1));
        setField(hackathon2, "id", 2L);
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon2, user);
        Submission submission = mockSubmission(hackathon2, team, user);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon1));
        given(submissionRepository.findById(1L)).willReturn(Optional.of(submission));

        assertThatThrownBy(() -> submissionService.activateVersion(1L, 1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SUBMISSION_NOT_FOUND);
    }

    @Test
    @DisplayName("팀원이 아니면 활성화 불가")
    void activateVersion_notTeamMember() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission submission = mockSubmission(hackathon, team, user);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findById(1L)).willReturn(Optional.of(submission));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 2L)).willReturn(false);

        assertThatThrownBy(() -> submissionService.activateVersion(1L, 1L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // deleteMySubmission
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("전체 제출 취소 성공 - 모든 버전 삭제")
    void deleteMySubmission_success() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission v1 = mockSubmission(hackathon, team, user, 1, false);
        Submission v2 = mockSubmission(hackathon, team, user, 2, true);

        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findAllMySubmissions(1L, 1L)).willReturn(List.of(v2, v1));
        given(teamMemberRepository.existsByTeamIdAndUserId(1L, 1L)).willReturn(true);

        submissionService.deleteMySubmission(1L, 1L);

        verify(submissionItemRepository, times(2)).deleteAllBySubmissionId(any());
        verify(submissionRepository).deleteAll(any());
    }

    @Test
    @DisplayName("제출 없으면 취소 불가")
    void deleteMySubmission_notFound() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        given(hackathonRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(hackathon));
        given(submissionRepository.findAllMySubmissions(1L, 1L)).willReturn(List.of());

        assertThatThrownBy(() -> submissionService.deleteMySubmission(1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.SUBMISSION_NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // getAdminSubmissions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("관리자 제출물 목록 - reviewStatus pending")
    void getAdminSubmissions_pending() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission submission = mockSubmission(hackathon, team, user);

        given(submissionRepository.findAllLatest(1L, null, PageRequest.of(0, 20)))
                .willReturn(new PageImpl<>(List.of(submission)));
        given(submissionItemRepository.findAllBySubmissionId(1L)).willReturn(List.of());
        given(judgeEvaluationRepository.existsByHackathonIdAndTeamId(1L, 1L)).willReturn(false);

        PageResponse<AdminSubmissionResponse> response =
                submissionService.getAdminSubmissions(1L, null, PageRequest.of(0, 20));

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getReviewStatus()).isEqualTo("pending");
        assertThat(response.getItems().get(0).getHackathonName()).isEqualTo("Test Hackathon");
        assertThat(response.getItems().get(0).getTeamName()).isEqualTo("Team 1");
    }

    @Test
    @DisplayName("관리자 제출물 목록 - reviewStatus reviewed")
    void getAdminSubmissions_reviewed() {
        Hackathon hackathon = mockHackathon(LocalDateTime.now().plusDays(1));
        User user = mockUser(1L);
        Team team = mockTeam(1L, hackathon, user);
        Submission submission = mockSubmission(hackathon, team, user);

        given(submissionRepository.findAllLatest(1L, null, PageRequest.of(0, 20)))
                .willReturn(new PageImpl<>(List.of(submission)));
        given(submissionItemRepository.findAllBySubmissionId(1L)).willReturn(List.of());
        given(judgeEvaluationRepository.existsByHackathonIdAndTeamId(1L, 1L)).willReturn(true);

        PageResponse<AdminSubmissionResponse> response =
                submissionService.getAdminSubmissions(1L, null, PageRequest.of(0, 20));

        assertThat(response.getItems().get(0).getReviewStatus()).isEqualTo("reviewed");
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
