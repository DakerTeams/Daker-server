package com.daker.domain.chat.repository;

import com.daker.domain.chat.domain.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    boolean existsByHackathonIdAndUserId(Long hackathonId, Long userId);

    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.hackathon WHERE cp.user.id = :userId ORDER BY cp.joinedAt DESC")
    List<ChatParticipant> findByUserIdWithHackathon(@Param("userId") Long userId);
}
