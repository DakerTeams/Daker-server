package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PrizeRepository extends JpaRepository<Prize, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Prize p")
    long sumAllAmounts();
}