package com.daker.domain.user.service;

import com.daker.domain.user.domain.User;
import com.daker.domain.user.dto.RoleUpdateRequest;
import com.daker.domain.user.dto.RoleUpdateResponse;
import com.daker.domain.user.repository.UserRepository;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public RoleUpdateResponse updateRole(Long targetUserId, RoleUpdateRequest request) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updateRole(request.getRole());

        return new RoleUpdateResponse(user);
    }
}
