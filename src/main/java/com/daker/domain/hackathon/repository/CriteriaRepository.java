package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {

    List<Criteria> findAllByHackathonId(Long hackathonId);
}
