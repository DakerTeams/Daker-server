package com.daker.domain.vote.repository;

import com.daker.domain.vote.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByHackathonIdAndVoterId(Long hackathonId, Long voterId);

    long countByHackathonIdAndTeamId(Long hackathonId, Long teamId);

    // 해커톤 전체 팀별 득표수 조회 (리더보드용)
    @Query("SELECT v.team.id, COUNT(v) FROM Vote v WHERE v.hackathon.id = :hackathonId GROUP BY v.team.id")
    List<Object[]> countByHackathonIdGroupByTeam(@Param("hackathonId") Long hackathonId);
}
