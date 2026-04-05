package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.HackathonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {

    Optional<Hackathon> findByIdAndDeletedFalse(Long id);
    List<Hackathon> findAllByDeletedFalse();
    Page<Hackathon> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT DISTINCT h FROM Hackathon h " +
           "LEFT JOIN h.hackathonTags ht " +
           "LEFT JOIN ht.tag t " +
           "WHERE h.deleted = false " +
           "AND (:status IS NULL OR h.status = :status) " +
           "AND (:excludeEnded = false OR h.status <> 'ENDED') " +
           "AND (:tag IS NULL OR t.name = :tag) " +
           "AND (:q IS NULL OR h.title LIKE %:q% OR h.organizer LIKE %:q%)")
    Page<Hackathon> findAllWithFilters(
            @Param("status") HackathonStatus status,
            @Param("excludeEnded") boolean excludeEnded,
            @Param("tag") String tag,
            @Param("q") String q,
            Pageable pageable
    );

    long countByStatusAndDeletedFalse(HackathonStatus status);

    long countByCreatedAtAfterAndDeletedFalse(LocalDateTime dateTime);

    @Modifying
    @Query("UPDATE Hackathon h SET h.status = 'UPCOMING' WHERE h.deleted = false AND h.status <> 'UPCOMING' AND :now < h.registrationStartDate")
    int updateToUpcoming(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Hackathon h SET h.status = 'OPEN' WHERE h.deleted = false AND h.status <> 'OPEN' AND :now >= h.registrationStartDate AND :now < h.registrationEndDate")
    int updateToOpen(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Hackathon h SET h.status = 'CLOSED' WHERE h.deleted = false AND h.status <> 'CLOSED' AND :now >= h.registrationEndDate AND :now < h.endDate")
    int updateToClosed(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Hackathon h SET h.status = 'ENDED' WHERE h.deleted = false AND h.status <> 'ENDED' AND :now >= h.endDate")
    int updateToEnded(@Param("now") LocalDateTime now);

}
