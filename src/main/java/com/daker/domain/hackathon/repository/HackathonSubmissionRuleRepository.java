package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.HackathonSubmissionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HackathonSubmissionRuleRepository extends JpaRepository<HackathonSubmissionRule, Long> {

    List<HackathonSubmissionRule> findByHackathonIdOrderBySortOrderAsc(Long hackathonId);
}
