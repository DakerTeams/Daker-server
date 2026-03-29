package com.daker.domain.team.repository;

import com.daker.domain.team.domain.ApplicationStatus;
import com.daker.domain.team.domain.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {

    List<TeamApplication> findAllByTeamIdAndStatus(Long teamId, ApplicationStatus status);

    Optional<TeamApplication> findByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, Long userId, ApplicationStatus status);

    void deleteAllByTeamId(Long teamId);
}
