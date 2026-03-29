package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.HackathonJudge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HackathonJudgeRepository extends JpaRepository<HackathonJudge, Long> {

    List<HackathonJudge> findAllByUserId(Long userId);

    Page<HackathonJudge> findAllByHackathonId(Long hackathonId, Pageable pageable);

    Optional<HackathonJudge> findByHackathonIdAndUserId(Long hackathonId, Long userId);

    boolean existsByHackathonIdAndUserId(Long hackathonId, Long userId);

    void deleteByHackathonIdAndUserId(Long hackathonId, Long userId);
}
