package com.daker.domain.chat.repository;

import com.daker.domain.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Slice<ChatMessage> findByHackathonIdOrderByCreatedAtDesc(Long hackathonId, Pageable pageable);
}
