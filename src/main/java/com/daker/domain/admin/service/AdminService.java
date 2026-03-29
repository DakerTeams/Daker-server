package com.daker.domain.admin.service;

import com.daker.domain.admin.dto.*;
import com.daker.domain.hackathon.domain.*;
import com.daker.domain.hackathon.repository.*;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.daker.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final HackathonRepository hackathonRepository;
    private final TagRepository tagRepository;
    private final HackathonTagRepository hackathonTagRepository;
    private final HackathonJudgeRepository hackathonJudgeRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    // -------------------------------------------------------------------------
    // 대시보드
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(int page, int limit) {
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();

        // 해커톤 통계
        List<Hackathon> all = hackathonRepository.findAll();
        long total = all.size();
        long active = all.stream().filter(h -> h.getStatus() == HackathonStatus.OPEN).count();
        long upcoming = all.stream().filter(h -> h.getStatus() == HackathonStatus.UPCOMING).count();
        long closed = all.stream().filter(h -> h.getStatus() == HackathonStatus.CLOSED).count();
        long ended = all.stream().filter(h -> h.getStatus() == HackathonStatus.ENDED).count();
        long newThisMonth = hackathonRepository.countByCreatedAtAfter(startOfMonth);

        // 해커톤 목록 (페이지네이션)
        Page<Hackathon> hackathonPage = hackathonRepository.findAll(
                PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id")));
        List<AdminDashboardResponse.HackathonItem> items = hackathonPage.getContent().stream()
                .map(h -> new AdminDashboardResponse.HackathonItem(h, teamRepository.findAllByHackathonId(h.getId()).size()))
                .toList();

        // 팀 통계
        long totalTeams = teamRepository.count();
        long newTeamsThisWeek = teamRepository.countByCreatedAtAfter(startOfWeek);

        // 유저 통계
        long totalUsers = userRepository.count();
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
        long totalJudges = userRepository.findAllByRole(Role.JUDGE, Pageable.unpaged()).getTotalElements();

        return AdminDashboardResponse.builder()
                .hackathons(AdminDashboardResponse.HackathonStats.builder()
                        .total(total)
                        .active(active)
                        .upcoming(upcoming)
                        .closed(closed)
                        .ended(ended)
                        .newThisMonth(newThisMonth)
                        .hackathonList(AdminDashboardResponse.HackathonList.builder()
                                .items(items)
                                .totalCount(hackathonPage.getTotalElements())
                                .page(page)
                                .limit(limit)
                                .build())
                        .build())
                .participatedTeams(AdminDashboardResponse.TeamStats.builder()
                        .total(totalTeams)
                        .newThisWeek(newTeamsThisWeek)
                        .build())
                .users(AdminDashboardResponse.UserStats.builder()
                        .total(totalUsers)
                        .newThisMonth(newUsersThisMonth)
                        .judges(totalJudges)
                        .build())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // 해커톤 관리
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<AdminHackathonResponse> getHackathons(Pageable pageable) {
        return new PageResponse<>(hackathonRepository.findAll(pageable).map(AdminHackathonResponse::new));
    }

    @Transactional
    public AdminHackathonCreateResponse createHackathon(HackathonCreateRequest request) {
        Hackathon hackathon = Hackathon.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .organizer(request.getOrganizerName())
                .status(HackathonStatus.UPCOMING)
                .scoreType(request.getScoreType())
                .startDate(request.getStartAt())
                .endDate(request.getEndAt())
                .registrationStartDate(request.getRegistrationStartAt())
                .registrationEndDate(request.getRegistrationEndAt())
                .submissionDeadlineAt(request.getSubmissionDeadlineAt())
                .maxTeamSize(request.getMaxTeamSize())
                .maxParticipants(request.getMaxParticipants())
                .campEnabled(request.isCampEnabled())
                .allowSolo(request.isAllowSolo())
                .build();

        hackathonRepository.save(hackathon);

        saveTags(hackathon, request.getTags());
        saveMilestones(hackathon, request.getMilestones());
        savePrizes(hackathon, request.getPrizes());
        saveCriteria(hackathon, request.getCriteria());
        saveNotices(hackathon, request.getNotices());
        saveLinks(hackathon, request.getLinks());

        return new AdminHackathonCreateResponse(hackathon);
    }

    @Transactional
    public AdminHackathonUpdateResponse updateHackathon(Long hackathonId, HackathonUpdateRequest request) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        hackathon.update(
                request.getTitle() != null ? request.getTitle() : hackathon.getTitle(),
                request.getSummary() != null ? request.getSummary() : hackathon.getSummary(),
                request.getDescription() != null ? request.getDescription() : hackathon.getDescription(),
                request.getThumbnailUrl() != null ? request.getThumbnailUrl() : hackathon.getThumbnailUrl(),
                request.getOrganizerName() != null ? request.getOrganizerName() : hackathon.getOrganizer(),
                request.getScoreType() != null ? request.getScoreType() : hackathon.getScoreType(),
                request.getStartAt() != null ? request.getStartAt() : hackathon.getStartDate(),
                request.getEndAt() != null ? request.getEndAt() : hackathon.getEndDate(),
                request.getRegistrationStartAt() != null ? request.getRegistrationStartAt() : hackathon.getRegistrationStartDate(),
                request.getRegistrationEndAt() != null ? request.getRegistrationEndAt() : hackathon.getRegistrationEndDate(),
                request.getSubmissionDeadlineAt() != null ? request.getSubmissionDeadlineAt() : hackathon.getSubmissionDeadlineAt(),
                request.getMaxTeamSize() != null ? request.getMaxTeamSize() : hackathon.getMaxTeamSize(),
                request.getMaxParticipants() != null ? request.getMaxParticipants() : hackathon.getMaxParticipants(),
                request.getCampEnabled() != null ? request.getCampEnabled() : hackathon.isCampEnabled(),
                request.getAllowSolo() != null ? request.getAllowSolo() : hackathon.isAllowSolo()
        );

        if (request.getTags() != null) {
            hackathonTagRepository.deleteAllByHackathonId(hackathonId);
            saveTags(hackathon, request.getTags());
        }
        if (request.getMilestones() != null) {
            hackathon.getMilestones().clear();
            saveMilestones(hackathon, request.getMilestones());
        }
        if (request.getPrizes() != null) {
            hackathon.getPrizes().clear();
            savePrizes(hackathon, request.getPrizes());
        }
        if (request.getCriteria() != null) {
            hackathon.getCriteriaList().clear();
            saveCriteria(hackathon, request.getCriteria());
        }
        if (request.getNotices() != null) {
            hackathon.getNotices().clear();
            saveNotices(hackathon, request.getNotices());
        }
        if (request.getLinks() != null) {
            hackathon.getLinks().clear();
            saveLinks(hackathon, request.getLinks());
        }

        return new AdminHackathonUpdateResponse(hackathon);
    }

    @Transactional
    public AdminHackathonCloseResponse closeHackathon(Long hackathonId) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        hackathon.updateStatus(HackathonStatus.CLOSED);
        return new AdminHackathonCloseResponse(hackathon);
    }

    // -------------------------------------------------------------------------
    // 유저 관리
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(Role role, Pageable pageable) {
        Page<User> users = role != null
                ? userRepository.findAllByRole(role, pageable)
                : userRepository.findAll(pageable);
        return new PageResponse<>(users.map(AdminUserResponse::new));
    }

    // -------------------------------------------------------------------------
    // 심사위원 관리
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<AdminJudgeResponse> getJudges(Long hackathonId, Pageable pageable) {
        Page<User> judges = userRepository.findAllByRole(Role.JUDGE, pageable);
        return new PageResponse<>(judges.map(user -> {
            List<HackathonJudge> assignments = hackathonJudgeRepository.findAllByUserId(user.getId());
            return new AdminJudgeResponse(user, assignments);
        }));
    }

    @Transactional
    public AdminUserResponse updateJudgeRole(Long userId, JudgeRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if ("grant".equalsIgnoreCase(request.getAction())) {
            if (user.getRole() == Role.JUDGE) {
                throw new CustomException(ErrorCode.ALREADY_A_JUDGE);
            }
            user.updateRole(Role.JUDGE);
        } else if ("revoke".equalsIgnoreCase(request.getAction())) {
            if (user.getRole() != Role.JUDGE) {
                throw new CustomException(ErrorCode.NOT_A_JUDGE);
            }
            user.updateRole(Role.USER);
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        return new AdminUserResponse(user);
    }

    @Transactional
    public void assignJudge(Long hackathonId, Long userId) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.JUDGE) {
            throw new CustomException(ErrorCode.NOT_A_JUDGE);
        }

        if (hackathonJudgeRepository.existsByHackathonIdAndUserId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.JUDGE_ALREADY_ASSIGNED);
        }

        hackathonJudgeRepository.save(HackathonJudge.builder()
                .hackathon(hackathon)
                .user(user)
                .build());
    }

    @Transactional
    public void removeJudge(Long hackathonId, Long userId) {
        if (!hackathonJudgeRepository.existsByHackathonIdAndUserId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.JUDGE_NOT_ASSIGNED);
        }
        hackathonJudgeRepository.deleteByHackathonIdAndUserId(hackathonId, userId);
    }

    // -------------------------------------------------------------------------
    // 헬퍼
    // -------------------------------------------------------------------------

    private void saveTags(Hackathon hackathon, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;
        tagNames.forEach(name -> {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(new Tag(name)));
            hackathonTagRepository.save(HackathonTag.builder().hackathon(hackathon).tag(tag).build());
        });
    }

    private void saveMilestones(Hackathon hackathon, List<HackathonCreateRequest.MilestoneRequest> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(m -> hackathon.getMilestones().add(
                Milestone.builder().hackathon(hackathon).title(m.getLabel()).date(m.getDate()).build()
        ));
    }

    private void savePrizes(Hackathon hackathon, List<HackathonCreateRequest.PrizeRequest> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(p -> hackathon.getPrizes().add(
                Prize.builder().hackathon(hackathon).ranking(p.getRank()).amount(0).description(p.getLabel() + " " + p.getAmount()).build()
        ));
    }

    private void saveCriteria(Hackathon hackathon, List<HackathonCreateRequest.CriteriaRequest> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(c -> hackathon.getCriteriaList().add(
                Criteria.builder().hackathon(hackathon).name(c.getLabel()).description(c.getWeight()).maxScore(c.getMaxScore() != null ? c.getMaxScore() : 0).build()
        ));
    }

    private void saveNotices(Hackathon hackathon, List<HackathonCreateRequest.NoticeRequest> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(n -> hackathon.getNotices().add(
                HackathonNotice.builder().hackathon(hackathon).content(n.getContent()).build()
        ));
    }

    private void saveLinks(Hackathon hackathon, List<HackathonCreateRequest.LinkRequest> list) {
        if (list == null || list.isEmpty()) return;
        list.forEach(l -> hackathon.getLinks().add(
                HackathonLink.builder().hackathon(hackathon).linkType(l.getLinkType()).label(l.getLabel()).url(l.getUrl()).build()
        ));
    }
}
