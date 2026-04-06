package com.daker.domain.team.repository;

import com.daker.domain.team.domain.TeamPrivateInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamPrivateInfoRepository extends JpaRepository<TeamPrivateInfo, Long> {
    Optional<TeamPrivateInfo> findByTeamId(Long teamId);
}
