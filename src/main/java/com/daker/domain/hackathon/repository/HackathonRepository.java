package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {

    Optional<Hackathon> findByIdAndDeletedFalse(Long id);

    @Query("SELECT DISTINCT h FROM Hackathon h " +
           "LEFT JOIN h.hackathonTags ht " +
           "LEFT JOIN ht.tag t " +
           "WHERE h.deleted = false " +
           "AND (:status IS NULL OR h.status = :status) " +
           "AND (:tag IS NULL OR t.name = :tag) " +
           "AND (:q IS NULL OR h.title LIKE %:q% OR h.organizer LIKE %:q%)")
    Page<Hackathon> findAllWithFilters(
            @Param("status") HackathonStatus status,
            @Param("tag") String tag,
            @Param("q") String q,
            Pageable pageable
    );

    // 어드민 전체 목록 (삭제 포함)
    Page<Hackathon> findAll(Pageable pageable);
}
