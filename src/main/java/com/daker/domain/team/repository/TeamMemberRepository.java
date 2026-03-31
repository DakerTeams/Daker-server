package com.daker.domain.team.repository;

import com.daker.domain.team.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findAllByTeamId(Long teamId);

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    // 해당 해커톤에 이미 팀원으로 속해있는지 확인
    @Query("SELECT COUNT(tm) > 0 FROM TeamMember tm " +
           "WHERE tm.user.id = :userId AND tm.team.hackathon.id = :hackathonId")
    boolean existsByUserIdAndHackathonId(
            @Param("userId") Long userId,
            @Param("hackathonId") Long hackathonId
    );

    @Query("SELECT tm FROM TeamMember tm " +
           "JOIN FETCH tm.user u " +
           "JOIN FETCH tm.team t " +
           "JOIN FETCH t.hackathon h " +
           "WHERE tm.leftAt IS NULL " +
           "AND t.status <> com.daker.domain.team.domain.TeamStatus.DELETED " +
           "AND h.deleted = false")
    List<TeamMember> findAllActiveMembersForRanking();
}
