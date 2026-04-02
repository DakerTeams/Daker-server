package com.daker.domain.user.dto;

import com.daker.domain.user.domain.UserTag;
import lombok.Getter;

@Getter
public class UserTagResponse {

    private final Long tagId;
    private final String name;

    public UserTagResponse(UserTag userTag) {
        this.tagId = userTag.getTag().getId();
        this.name = userTag.getTag().getName();
    }
}
