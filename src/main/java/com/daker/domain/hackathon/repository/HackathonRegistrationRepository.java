package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.HackathonRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HackathonRegistrationRepository extends JpaRepository<HackathonRegistration, Long> {

    List<HackathonRegistration> findAllByHackathonId(Long hackathonId);

    Optional<HackathonRegistration> findByHackathonIdAndTeamId(Long hackathonId, Long teamId);

    Optional<HackathonRegistration> findByTeamId(Long teamId);

    boolean existsByHackathonIdAndTeamId(Long hackathonId, Long teamId);
}
