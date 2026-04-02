package com.daker.domain.xp.service;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.ScoreType;
import com.daker.domain.judge.domain.JudgeEvaluation;
import com.daker.domain.judge.repository.JudgeEvaluationRepository;
import com.daker.domain.submission.repository.SubmissionRepository;
import com.daker.domain.team.domain.Team;
import com.daker.domain.team.repository.TeamMemberRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.vote.repository.VoteRepository;
import com.daker.domain.xp.domain.UserXpHistory;
import com.daker.domain.xp.domain.XpType;
import com.daker.domain.xp.repository.UserXpHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class XpService {

    private final UserXpHistoryRepository userXpHistoryRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final SubmissionRepository submissionRepository;
    private final JudgeEvaluationRepository judgeEvaluationRepository;
    private final VoteRepository voteRepository;

    @Transactional
    public void awardHackathonXp(Hackathon hackathon) {
        List<Long> topTeamIds = resolveTopThreeTeams(hackathon);
        if (topTeamIds.isEmpty()) {
            log.info("Hackathon {} has no eligible teams for XP awards", hackathon.getId());
            return;
        }

        XpType[] types = {XpType.AWARD_1ST, XpType.AWARD_2ND, XpType.AWARD_3RD};
        for (int i = 0; i < topTeamIds.size(); i++) {
            Long teamId = topTeamIds.get(i);
            XpType type = types[i];

            teamMemberRepository.findAllByTeamId(teamId).forEach(member -> {
                if (!userXpHistoryRepository.existsByUserIdAndHackathonIdAndType(
                        member.getUser().getId(), hackathon.getId(), type)) {
                    userXpHistoryRepository.save(UserXpHistory.builder()
                            .user(member.getUser())
                            .hackathon(hackathon)
                            .type(type)
                            .build());
                    log.info("Awarded {} XP ({}) to user {} for hackathon {}",
                            type.getAmount(), type, member.getUser().getId(), hackathon.getId());
                }
            });
        }
    }

    private List<Long> resolveTopThreeTeams(Hackathon hackathon) {
        List<Team> teamsWithSubmission = teamRepository.findAllByHackathonId(hackathon.getId()).stream()
                .filter(t -> submissionRepository
                        .findByTeamIdAndHackathonIdAndIsLatestTrue(t.getId(), hackathon.getId())
                        .isPresent())
                .collect(Collectors.toList());

        if (teamsWithSubmission.isEmpty()) return List.of();

        if (hackathon.getScoreType() == ScoreType.VOTE) {
            return resolveByVote(hackathon.getId(), teamsWithSubmission);
        } else {
            return resolveByJudgeScore(hackathon.getId(), teamsWithSubmission);
        }
    }

    private List<Long> resolveByVote(Long hackathonId, List<Team> teams) {
        Map<Long, Long> voteCounts = voteRepository.countByHackathonIdGroupByTeam(hackathonId)
                .stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return teams.stream()
                .sorted(Comparator.comparingLong((Team t) -> voteCounts.getOrDefault(t.getId(), 0L)).reversed())
                .limit(3)
                .map(Team::getId)
                .collect(Collectors.toList());
    }

    private List<Long> resolveByJudgeScore(Long hackathonId, List<Team> teams) {
        Map<Long, Double> avgScores = new HashMap<>();
        for (Team team : teams) {
            List<JudgeEvaluation> evals =
                    judgeEvaluationRepository.findAllByHackathonIdAndTeamId(hackathonId, team.getId());
            if (!evals.isEmpty()) {
                double avg = evals.stream().mapToDouble(JudgeEvaluation::getTotalScore).average().orElse(0);
                avgScores.put(team.getId(), avg);
            }
        }

        return teams.stream()
                .filter(t -> avgScores.containsKey(t.getId()))
                .sorted(Comparator.comparingDouble((Team t) -> avgScores.getOrDefault(t.getId(), 0.0)).reversed())
                .limit(3)
                .map(Team::getId)
                .collect(Collectors.toList());
    }
}
