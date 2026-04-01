package com.daker.domain.submission.repository;

import com.daker.domain.submission.domain.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByTeamIdAndHackathonIdAndIsLatestTrue(Long teamId, Long hackathonId);

    @Query("SELECT s FROM Submission s " +
           "WHERE s.isLatest = true " +
           "AND (:hackathonId IS NULL OR s.hackathon.id = :hackathonId) " +
           "AND (:teamId IS NULL OR s.team.id = :teamId)")
    Page<Submission> findAllLatest(
            @Param("hackathonId") Long hackathonId,
            @Param("teamId") Long teamId,
            Pageable pageable
    );

    // 내가 속한 팀들 중 해당 해커톤에 제출한 최신 제출물 조회
    @Query("SELECT s FROM Submission s " +
           "WHERE s.hackathon.id = :hackathonId " +
           "AND s.isLatest = true " +
           "AND EXISTS (SELECT tm FROM TeamMember tm WHERE tm.team = s.team AND tm.user.id = :userId)")
    Optional<Submission> findMyLatestSubmission(
            @Param("hackathonId") Long hackathonId,
            @Param("userId") Long userId
    );

    // 내 팀의 전체 제출 이력 (버전 목록)
    @Query("SELECT s FROM Submission s " +
           "WHERE s.hackathon.id = :hackathonId " +
           "AND EXISTS (SELECT tm FROM TeamMember tm WHERE tm.team = s.team AND tm.user.id = :userId) " +
           "ORDER BY s.revisionNo DESC")
    List<Submission> findAllMySubmissions(
            @Param("hackathonId") Long hackathonId,
            @Param("userId") Long userId
    );

    // 팀+해커톤의 전체 제출 (삭제용)
    List<Submission> findAllByTeamIdAndHackathonId(Long teamId, Long hackathonId);

    // 팀+해커톤 is_latest 일괄 false 처리
    @Modifying
    @Query("UPDATE Submission s SET s.isLatest = false " +
           "WHERE s.team.id = :teamId AND s.hackathon.id = :hackathonId")
    void deactivateAllByTeamIdAndHackathonId(
            @Param("teamId") Long teamId,
            @Param("hackathonId") Long hackathonId
    );
}
