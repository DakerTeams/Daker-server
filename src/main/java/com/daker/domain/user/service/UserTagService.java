package com.daker.domain.user.service;

import com.daker.domain.hackathon.domain.Hackathon;
import com.daker.domain.hackathon.domain.Tag;
import com.daker.domain.hackathon.dto.HackathonSummaryResponse;
import com.daker.domain.hackathon.repository.HackathonRepository;
import com.daker.domain.hackathon.repository.HackathonTagRepository;
import com.daker.domain.hackathon.repository.TagRepository;
import com.daker.domain.team.repository.TeamRepository;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.domain.UserTag;
import com.daker.domain.user.dto.UserTagResponse;
import com.daker.domain.user.repository.UserRepository;
import com.daker.domain.user.repository.UserTagRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTagService {

    private final UserTagRepository userTagRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final HackathonRepository hackathonRepository;
    private final HackathonTagRepository hackathonTagRepository;
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<UserTagResponse> getTags(Long userId) {
        return userTagRepository.findAllByUserId(userId).stream()
                .map(UserTagResponse::new)
                .toList();
    }

    @Transactional
    public UserTagResponse addTag(Long userId, String tagName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Tag tag = tagRepository.findByName(tagName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName)));

        if (userTagRepository.existsByUserIdAndTagId(userId, tag.getId())) {
            throw new CustomException(ErrorCode.DUPLICATE_TAG);
        }

        UserTag userTag = UserTag.builder().user(user).tag(tag).build();
        return new UserTagResponse(userTagRepository.save(userTag));
    }

    @Transactional
    public void removeTag(Long userId, Long tagId) {
        if (!userTagRepository.existsByUserIdAndTagId(userId, tagId)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        userTagRepository.deleteByUserIdAndTagId(userId, tagId);
    }

    @Transactional(readOnly = true)
    public List<HackathonSummaryResponse> getRecommendedHackathons(Long userId) {
        List<Long> hackathonIds = userTagRepository.findMatchingHackathonIds(userId);
        if (hackathonIds.isEmpty()) {
            return List.of();
        }

        return hackathonRepository.findAllById(hackathonIds).stream()
                .filter(h -> !h.isDeleted())  // Lombok @Getter generates isDeleted() for boolean
                .map(h -> {
                    List<String> tags = hackathonTagRepository.findAllByHackathonId(h.getId())
                            .stream().map(ht -> ht.getTag().getName()).toList();
                    int participants = teamRepository.findAllByHackathonId(h.getId())
                            .stream().mapToInt(t -> t.getMembers().size()).sum();
                    return new HackathonSummaryResponse(h, tags, participants);
                })
                .toList();
    }
}
