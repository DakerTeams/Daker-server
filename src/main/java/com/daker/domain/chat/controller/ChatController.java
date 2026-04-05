package com.daker.domain.chat.controller;

import com.daker.domain.chat.dto.ChatMessageRequest;
import com.daker.domain.chat.dto.ChatMessageResponse;
import com.daker.domain.chat.dto.ChatRoomResponse;
import com.daker.domain.chat.service.ChatService;
import com.daker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // STOMP 메시지 수신 → DB 저장 + 브로드캐스트
    // 클라이언트: SEND /app/hackathons/{hackathonId}/chat
    // 구독:       SUBSCRIBE /topic/hackathon/{hackathonId}
    @MessageMapping("/hackathons/{hackathonId}/chat")
    public void sendMessage(
            @DestinationVariable Long hackathonId,
            @Payload ChatMessageRequest request,
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());
        chatService.sendMessage(hackathonId, userId, request);
    }

    // 채팅방 참가
    @PostMapping("/hackathons/{hackathonId}/chat/join")
    public ApiResponse<Void> joinChat(
            @PathVariable Long hackathonId,
            @AuthenticationPrincipal Long userId
    ) {
        chatService.joinChat(hackathonId, userId);
        return ApiResponse.ok(null);
    }

    // 채팅방 나가기
    @DeleteMapping("/hackathons/{hackathonId}/chat/leave")
    public ApiResponse<Void> leaveChat(
            @PathVariable Long hackathonId,
            @AuthenticationPrincipal Long userId
    ) {
        chatService.leaveChat(hackathonId, userId);
        return ApiResponse.ok(null);
    }

    // 내가 참가한 채팅방 목록
    @GetMapping("/hackathons/me/chat")
    public ApiResponse<List<ChatRoomResponse>> getMyRooms(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.ok(chatService.getMyRooms(userId));
    }

    // 이전 메시지 조회 (최신순, cursor 방식)
    @GetMapping("/hackathons/{hackathonId}/chat/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(
            @PathVariable Long hackathonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return ApiResponse.ok(chatService.getMessages(hackathonId, page, size));
    }
}
