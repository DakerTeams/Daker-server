package com.daker.domain.chat.dto;

import com.daker.domain.chat.domain.ChatMessage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {

    private final Long messageId;
    private final Long hackathonId;
    private final Long senderId;
    private final String senderNickname;
    private final String content;
    private final LocalDateTime createdAt;

    public ChatMessageResponse(ChatMessage message) {
        this.messageId = message.getId();
        this.hackathonId = message.getHackathon().getId();
        this.senderId = message.getSender().getId();
        this.senderNickname = message.getSender().getNickname();
        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
    }
}
