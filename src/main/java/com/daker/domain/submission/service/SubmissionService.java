package com.daker.domain.submission.service;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.judge.repository.JudgeEvaluationRepository;
import com.daker.domain.submission.domain.Submission;
import com.daker.domain.submission.domain.SubmissionItem;
import com.daker.domain.submission.domain.SubmissionStatus;
import com.daker.domain.submission.dto.AdminSubmissionHackathonSummaryResponse;
import com.daker.domain.submission.dto.AdminSubmissionResponse;
import com.daker.domain.submission.dto.DownloadFilePayload;
import com.daker.domain.submission.dto.SubmissionCreateResponse;
import com.daker.domain.submission.dto.SubmissionHistoryResponse;
import com.daker.domain.submission.dto.SubmissionStatusResponse;
import com.daker.domain.submission.repository.SubmissionItemRepository;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.daker.global.infra.S3Uploader;
import com.daker.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionItemRepository submissionItemRepository;
    private final HackathonRepository hackathonRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final JudgeEvaluationRepository judgeEvaluationRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public SubmissionCreateResponse submit(Long hackathonId, Long userId,
                                           MultipartFile file, String memo, Long teamId) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        if (hackathon.getSubmissionDeadlineAt() != null
                && LocalDateTime.now().isAfter(hackathon.getSubmissionDeadlineAt())) {
            throw new CustomException(ErrorCode.SUBMISSION_DEADLINE_EXCEEDED);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 기존 최신 제출이 있으면 비활성화 후 revision 증가
        int nextRevision = 1;
        Optional<Submission> current = submissionRepository.findByTeamIdAndHackathonIdAndIsLatestTrue(teamId, hackathonId);
        if (current.isPresent()) {
            submissionRepository.deactivateAllByTeamIdAndHackathonId(teamId, hackathonId);
            nextRevision = current.get().getRevisionNo() + 1;
        }

        User submitter = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Submission submission = Submission.builder()
                .hackathon(hackathon)
                .team(team)
                .submitter(submitter)
                .status(SubmissionStatus.SUBMITTED)
                .revisionNo(nextRevision)
                .isLatest(true)
                .submittedAt(LocalDateTime.now())
                .build();

        submission = submissionRepository.save(submission);

        // memo → value_text 아이템 저장
        if (memo != null && !memo.isBlank()) {
            submissionItemRepository.save(SubmissionItem.builder()
                    .submission(submission)
                    .valueText(memo)
                    .isFinal(true)
                    .build());
        }

        // 파일 → S3 업로드 후 key를 file_name에 저장
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                    : "";
            String s3Key = s3Uploader.upload(file,
                    "submissions/hackathon-" + hackathonId + "/team-" + teamId);
            submissionItemRepository.save(SubmissionItem.builder()
                    .submission(submission)
                    .fileName(s3Key)
                    .originalFileName(originalName)
                    .fileExtension(extension)
                    .fileSize(file.getSize())
                    .isFinal(true)
                    .build());
        }

        return new SubmissionCreateResponse(submission);
    }

    @Transactional(readOnly = true)
    public SubmissionStatusResponse getMySubmissionStatus(Long hackathonId, Long userId) {
        hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        return submissionRepository.findMyLatestSubmission(hackathonId, userId)
                .map(submission -> {
                    List<SubmissionItem> items = submissionItemRepository.findAllBySubmissionId(submission.getId());
                    return new SubmissionStatusResponse(submission, items);
                })
                .orElse(SubmissionStatusResponse.notSubmitted());
    }

    @Transactional(readOnly = true)
    public List<SubmissionHistoryResponse> getMySubmissionHistory(Long hackathonId, Long userId) {
        hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        return submissionRepository.findAllMySubmissions(hackathonId, userId).stream()
                .map(s -> new SubmissionHistoryResponse(s, submissionItemRepository.findAllBySubmissionId(s.getId())))
                .toList();
    }

    @Transactional
    public SubmissionStatusResponse activateVersion(Long hackathonId, Long submissionId, Long userId) {
        hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        Submission target = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBMISSION_NOT_FOUND));

        if (!target.getHackathon().getId().equals(hackathonId)) {
            throw new CustomException(ErrorCode.SUBMISSION_NOT_FOUND);
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(target.getTeam().getId(), userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        submissionRepository.deactivateAllByTeamIdAndHackathonId(target.getTeam().getId(), hackathonId);
        target.activate();

        List<SubmissionItem> items = submissionItemRepository.findAllBySubmissionId(submissionId);
        return new SubmissionStatusResponse(target, items);
    }

    @Transactional
    public void deleteMySubmission(Long hackathonId, Long userId) {
        hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        List<Submission> all = submissionRepository.findAllMySubmissions(hackathonId, userId);
        if (all.isEmpty()) {
            throw new CustomException(ErrorCode.SUBMISSION_NOT_FOUND);
        }

        Long teamId = all.get(0).getTeam().getId();
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        all.forEach(s -> submissionItemRepository.deleteAllBySubmissionId(s.getId()));
        submissionRepository.deleteAll(all);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminSubmissionResponse> getAdminSubmissions(Long hackathonId, Long teamId, Pageable pageable) {
        Page<Submission> page = submissionRepository.findAllLatest(hackathonId, teamId, pageable);

        return new PageResponse<>(page.map(submission -> {
            List<SubmissionItem> items = submissionItemRepository.findAllBySubmissionId(submission.getId());
            boolean reviewed = judgeEvaluationRepository
                    .existsByHackathonIdAndTeamId(submission.getHackathon().getId(), submission.getTeam().getId());
            return new AdminSubmissionResponse(submission, items, reviewed);
        }));
    }

    @Transactional(readOnly = true)
    public List<AdminSubmissionHackathonSummaryResponse> getAdminSubmissionHackathons() {
        List<Submission> submissions = submissionRepository.findAllLatest(null, null, Pageable.unpaged()).getContent();
        Map<Long, List<Submission>> byHackathon = new LinkedHashMap<>();

        submissions.stream()
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .forEach(submission -> byHackathon
                        .computeIfAbsent(submission.getHackathon().getId(), ignored -> new java.util.ArrayList<>())
                        .add(submission));

        return byHackathon.values().stream()
                .map(items -> {
                    Submission first = items.get(0);
                    int totalFileCount = items.stream()
                            .mapToInt(submission -> submissionItemRepository.findAllBySubmissionId(submission.getId()).size())
                            .sum();

                    LocalDateTime latestSubmittedAt = items.stream()
                            .map(Submission::getSubmittedAt)
                            .filter(java.util.Objects::nonNull)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    return AdminSubmissionHackathonSummaryResponse.builder()
                            .hackathonId(first.getHackathon().getId())
                            .hackathonName(first.getHackathon().getTitle())
                            .submittedTeamCount(items.size())
                            .totalFileCount(totalFileCount)
                            .latestSubmittedAt(latestSubmittedAt)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminSubmissionResponse> getAdminSubmissionsByHackathon(Long hackathonId) {
        return submissionRepository.findAllLatest(hackathonId, null, Pageable.unpaged()).getContent().stream()
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .map(submission -> {
                    List<SubmissionItem> items = submissionItemRepository.findAllBySubmissionId(submission.getId());
                    boolean reviewed = judgeEvaluationRepository
                            .existsByHackathonIdAndTeamId(submission.getHackathon().getId(), submission.getTeam().getId());
                    return new AdminSubmissionResponse(submission, items, reviewed);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadFilePayload downloadSubmissionArchive(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBMISSION_NOT_FOUND));

        List<SubmissionItem> items = submissionItemRepository.findAllBySubmissionId(submissionId);
        byte[] bytes = buildSubmissionZip(
                submission.getHackathon().getTitle(),
                submission.getTeam().getName(),
                items
        );

        String safeHackathonName = sanitizeFileName(submission.getHackathon().getTitle());
        String safeTeamName = sanitizeFileName(submission.getTeam().getName());
        String fileName = safeHackathonName + "_" + safeTeamName + "_submission.zip";

        return new DownloadFilePayload(bytes, fileName, "application/zip");
    }

    @Transactional(readOnly = true)
    public DownloadFilePayload downloadHackathonSubmissionArchive(Long hackathonId) {
        List<Submission> submissions = submissionRepository.findAllLatest(hackathonId, null, Pageable.unpaged()).getContent();
        if (submissions.isEmpty()) {
            throw new CustomException(ErrorCode.SUBMISSION_NOT_FOUND);
        }

        String hackathonName = submissions.get(0).getHackathon().getTitle();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

            for (Submission submission : submissions) {
                List<SubmissionItem> items = submissionItemRepository.findAllBySubmissionId(submission.getId());
                addSubmissionEntriesToZip(
                        zipOutputStream,
                        sanitizeFileName(submission.getTeam().getName()),
                        items
                );
            }

            zipOutputStream.finish();
            String fileName = sanitizeFileName(hackathonName) + "_all_submissions.zip";
            return new DownloadFilePayload(outputStream.toByteArray(), fileName, "application/zip");
        } catch (IOException exception) {
            throw new RuntimeException("제출물 압축 다운로드에 실패했습니다.", exception);
        }
    }

    private byte[] buildSubmissionZip(String hackathonName, String teamName, List<SubmissionItem> items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

            addSubmissionEntriesToZip(zipOutputStream, sanitizeFileName(teamName), items);
            zipOutputStream.finish();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new RuntimeException(
                    "제출물 압축 다운로드에 실패했습니다. hackathon=" + hackathonName + ", team=" + teamName,
                    exception
            );
        }
    }

    private void addSubmissionEntriesToZip(ZipOutputStream zipOutputStream, String folderName, List<SubmissionItem> items)
            throws IOException {
        int linkIndex = 1;

        for (SubmissionItem item : items) {
            if (item.getFileName() != null && !item.getFileName().isBlank()) {
                String originalName = item.getOriginalFileName() != null && !item.getOriginalFileName().isBlank()
                        ? item.getOriginalFileName()
                        : "file-" + item.getId();
                ZipEntry zipEntry = new ZipEntry(folderName + "/" + sanitizeFileName(originalName));
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(s3Uploader.download(item.getFileName()));
                zipOutputStream.closeEntry();
            } else if (item.getValueUrl() != null && !item.getValueUrl().isBlank()) {
                String entryName = folderName + "/link-" + linkIndex++ + ".txt";
                ZipEntry zipEntry = new ZipEntry(entryName);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(item.getValueUrl().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
        }
    }

    private String sanitizeFileName(String value) {
        return value == null ? "unknown" : value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
