package com.daker.domain.judge.service;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonJudge;
import com.daker.domain.hackathon.domain.HackathonRegistration;
import com.daker.domain.hackathon.repository.HackathonJudgeRepository;
import com.daker.domain.hackathon.repository.HackathonRegistrationRepository;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.judge.domain.JudgeEvaluation;
import com.daker.domain.judge.dto.JudgeHackathonResponse;
import com.daker.domain.judge.dto.JudgeScoreRequest;
import com.daker.domain.judge.dto.JudgeScoreResponse;
import com.daker.domain.judge.dto.JudgeSubmissionResponse;
import com.daker.domain.judge.dto.JudgeTeamsResponse;
import com.daker.domain.judge.repository.JudgeEvaluationRepository;
import com.daker.domain.submission.domain.Submission;
import com.daker.domain.submission.repository.SubmissionItemRepository;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.team.domain.Team;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import com.daker.global.infra.S3Uploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JudgeService {

    private final HackathonJudgeRepository hackathonJudgeRepository;
    private final HackathonRepository hackathonRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final JudgeEvaluationRepository judgeEvaluationRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionItemRepository submissionItemRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public JudgeHackathonResponse getAssignedHackathons(Long userId) {
        List<HackathonJudge> assignments = hackathonJudgeRepository.findAllByUserId(userId);

        List<JudgeHackathonResponse.HackathonItem> items = assignments.stream()
                .map(assignment -> {
                    Hackathon hackathon = assignment.getHackathon();
                    long submissionCount = submissionRepository.findAllLatest(
                            hackathon.getId(),
                            null,
                            Pageable.unpaged()
                    ).getTotalElements();
                    long reviewedCount = judgeEvaluationRepository.countByHackathonIdAndJudgeId(hackathon.getId(), userId);
                    return new JudgeHackathonResponse.HackathonItem(hackathon, submissionCount, reviewedCount);
                })
                .toList();

        return new JudgeHackathonResponse(items);
    }

    @Transactional(readOnly = true)
    public JudgeTeamsResponse getTeamsForHackathon(Long hackathonId, Long userId) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        if (!hackathonJudgeRepository.existsByHackathonIdAndUserId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        List<HackathonRegistration> registrations = hackathonRegistrationRepository.findAllByHackathonId(hackathonId);

        List<JudgeTeamsResponse.TeamItem> teamItems = registrations.stream()
                .map(reg -> {
                    Team team = reg.getTeam();
                    Submission submission = submissionRepository
                            .findByTeamIdAndHackathonIdAndIsLatestTrue(team.getId(), hackathonId)
                            .orElse(null);
                    JudgeEvaluation evaluation = judgeEvaluationRepository
                            .findByHackathonIdAndTeamIdAndJudgeId(hackathonId, team.getId(), userId)
                            .orElse(null);
                    return new JudgeTeamsResponse.TeamItem(team, submission, evaluation);
                })
                .toList();

        return new JudgeTeamsResponse(hackathon, teamItems);
    }

    @Transactional(readOnly = true)
    public JudgeSubmissionResponse getSubmissionForTeam(Long hackathonId, Long teamId, Long userId) {
        if (!hackathonJudgeRepository.existsByHackathonIdAndUserId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (!hackathonRegistrationRepository.existsByHackathonIdAndTeamId(hackathonId, teamId)) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        Submission submission = submissionRepository
                .findByTeamIdAndHackathonIdAndIsLatestTrue(teamId, hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBMISSION_NOT_FOUND));

        var items = submissionItemRepository.findAllBySubmissionId(submission.getId());

        return new JudgeSubmissionResponse(submission, items, s3Uploader::getFileUrl);
    }

    @Transactional
    public JudgeScoreResponse score(Long hackathonId, Long teamId, Long userId, JudgeScoreRequest request) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        if (hackathon.getScoreType() != com.daker.domain.hackathon.domain.ScoreType.SCORE) {
            throw new CustomException(ErrorCode.SCORE_TYPE_MISMATCH);
        }

        if (hackathon.getCriteriaList() == null || hackathon.getCriteriaList().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if (!hackathonJudgeRepository.existsByHackathonIdAndUserId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (!hackathonRegistrationRepository.existsByHackathonIdAndTeamId(hackathonId, teamId)) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }

        User judge = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getScores() == null || request.getScores().size() != hackathon.getCriteriaList().size()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        double totalScore = request.getScores().stream()
                .mapToDouble(JudgeScoreRequest.ScoreItem::getScore)
                .sum();

        String scoresJson = toJson(request.getScores().stream()
                .map(JudgeScoreRequest.ScoreItem::getScore)
                .toList());

        JudgeEvaluation evaluation = judgeEvaluationRepository
                .findByHackathonIdAndTeamIdAndJudgeId(hackathonId, teamId, userId)
                .orElse(null);

        if (evaluation != null) {
            evaluation.update(totalScore, scoresJson);
        } else {
            Team team = hackathonRegistrationRepository.findByHackathonIdAndTeamId(hackathonId, teamId)
                    .map(HackathonRegistration::getTeam)
                    .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

            evaluation = judgeEvaluationRepository.save(JudgeEvaluation.builder()
                    .hackathon(hackathon)
                    .team(team)
                    .judge(judge)
                    .totalScore(totalScore)
                    .scoresJson(scoresJson)
                    .build());
        }

        return new JudgeScoreResponse(evaluation);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
}
