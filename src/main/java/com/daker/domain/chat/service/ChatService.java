package com.daker.domain.chat.service;

import com.daker.domain.chat.domain.ChatMessage;
import com.daker.domain.chat.domain.ChatParticipant;
import com.daker.domain.chat.dto.ChatMessageRequest;
import com.daker.domain.chat.dto.ChatMessageResponse;
import com.daker.domain.chat.dto.ChatRoomResponse;
import com.daker.domain.chat.repository.ChatMessageRepository;
import com.daker.domain.chat.repository.ChatParticipantRepository;
import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(Long hackathonId, Long userId, ChatMessageRequest request) {
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .hackathon(hackathon)
                .sender(sender)
                .content(request.getContent())
                .build());

        ChatMessageResponse response = new ChatMessageResponse(message);
        messagingTemplate.convertAndSend("/topic/hackathon/" + hackathonId, response);
        return response;
    }

    @Transactional
    public void joinChat(Long hackathonId, Long userId) {
        if (!hackathonRepository.existsById(hackathonId)) {
            throw new CustomException(ErrorCode.HACKATHON_NOT_FOUND);
        }
        if (chatParticipantRepository.existsByHackathonIdAndUserId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.ALREADY_JOINED_CHAT);
        }
        Hackathon hackathon = hackathonRepository.findByIdAndDeletedFalse(hackathonId)
                .orElseThrow(() -> new CustomException(ErrorCode.HACKATHON_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        chatParticipantRepository.save(ChatParticipant.builder()
                .hackathon(hackathon)
                .user(user)
                .build());
    }

    @Transactional
    public void leaveChat(Long hackathonId, Long userId) {
        if (!chatParticipantRepository.existsByHackathonIdAndUserId(hackathonId, userId)) {
            throw new CustomException(ErrorCode.CHAT_PARTICIPANT_NOT_FOUND);
        }
        chatParticipantRepository.deleteByHackathonIdAndUserId(hackathonId, userId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long userId) {
        return chatParticipantRepository.findByUserIdWithHackathon(userId).stream()
                .map(ChatRoomResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long hackathonId, int page, int size) {
        if (!hackathonRepository.existsById(hackathonId)) {
            throw new CustomException(ErrorCode.HACKATHON_NOT_FOUND);
        }

        Slice<ChatMessage> slice = chatMessageRepository
                .findByHackathonIdOrderByCreatedAtDesc(hackathonId, PageRequest.of(page, size));

        return slice.getContent().stream()
                .map(ChatMessageResponse::new)
                .toList();
    }
}
