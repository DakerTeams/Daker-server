package com.daker.domain.judge.repository;

import com.daker.domain.judge.domain.JudgeEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JudgeEvaluationRepository extends JpaRepository<JudgeEvaluation, Long> {

    List<JudgeEvaluation> findAllByHackathonIdAndTeamId(Long hackathonId, Long teamId);

    Optional<JudgeEvaluation> findByHackathonIdAndTeamIdAndJudgeId(Long hackathonId, Long teamId, Long judgeId);

    boolean existsByHackathonIdAndTeamIdAndJudgeId(Long hackathonId, Long teamId, Long judgeId);

    long countByHackathonIdAndJudgeId(Long hackathonId, Long judgeId);
}
