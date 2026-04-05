package com.daker.domain.chat.dto;

import com.daker.domain.chat.domain.ChatParticipant;
import com.daker.domain.hackathon.domain.Hackathon;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatRoomResponse {

    private final Long hackathonId;
    private final String hackathonTitle;
    private final String thumbnailUrl;
    private final LocalDateTime joinedAt;

    public ChatRoomResponse(ChatParticipant participant) {
        Hackathon hackathon = participant.getHackathon();
        this.hackathonId = hackathon.getId();
        this.hackathonTitle = hackathon.getTitle();
        this.thumbnailUrl = hackathon.getThumbnailUrl();
        this.joinedAt = participant.getJoinedAt();
    }
}
