package com.daker.domain.admin.dto;

import com.daker.domain.hackathon.domain.HackathonJudge;
import com.daker.domain.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AdminJudgeResponse {

    private final Long userId;
    private final String nickname;
    private final String email;
    private final List<String> assignedHackathons;
    private final LocalDateTime assignedAt;

    public AdminJudgeResponse(User user, List<HackathonJudge> assignments) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.assignedHackathons = assignments.stream()
                .map(j -> j.getHackathon().getTitle())
                .toList();
        this.assignedAt = assignments.isEmpty() ? null : assignments.get(0).getAssignedAt();
    }
}
