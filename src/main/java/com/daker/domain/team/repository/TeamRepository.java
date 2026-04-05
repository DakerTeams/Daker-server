package com.daker.domain.team.repository;

import com.daker.domain.team.domain.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"positions"})
    @Query("SELECT t FROM Team t WHERE t.id = :id")
    Optional<Team> findByIdWithDetails(@Param("id") Long id);

    // 팀 목록: hackathonId / isOpen / 키워드 필터
    @Query("SELECT t FROM Team t " +
           "WHERE t.status != com.daker.domain.team.domain.TeamStatus.DELETED " +
           "AND (:hackathonId IS NULL OR t.hackathon.id = :hackathonId) " +
           "AND (:isOpen IS NULL OR t.isOpen = :isOpen) " +
           "AND (:q IS NULL OR t.name LIKE %:q%)")
    Page<Team> findAllWithFilters(
            @Param("hackathonId") Long hackathonId,
            @Param("isOpen") Boolean isOpen,
            @Param("q") String q,
            Pageable pageable
    );

    // 내가 팀장이거나 멤버인 팀 목록
    @Query("SELECT t FROM Team t " +
           "WHERE t.leader.id = :userId " +
           "OR EXISTS (SELECT tm FROM TeamMember tm WHERE tm.team = t AND tm.user.id = :userId)")
    List<Team> findAllByUserId(@Param("userId") Long userId);

    // 해커톤 내 팀 목록 (리더보드용)
    List<Team> findAllByHackathonId(Long hackathonId);

    boolean existsByHackathonIdAndLeaderId(Long hackathonId, Long leaderId);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Modifying
    @Query("UPDATE Team t SET t.status = com.daker.domain.team.domain.TeamStatus.CLOSED, t.isOpen = false " +
           "WHERE t.hackathon.status = com.daker.domain.hackathon.domain.HackathonStatus.ENDED " +
           "AND t.status = com.daker.domain.team.domain.TeamStatus.OPEN")
    int closeTeamsOfFinishedHackathons();
}
