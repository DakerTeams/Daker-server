package com.daker.domain.user;

import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.dto.RoleUpdateRequest;
import com.daker.domain.user.dto.RoleUpdateResponse;
import com.daker.domain.user.repository.UserRepository;
import com.daker.domain.user.service.UserService;
import com.daker.global.exception.CustomException;
import com.daker.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User mockUser() {
        return User.builder()
                .email("test@test.com")
                .nickname("tester")
                .password("encoded_pw")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("역할 변경 성공")
    void updateRole_success() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        setField(request, "role", Role.JUDGE);

        User user = mockUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        RoleUpdateResponse response = userService.updateRole(1L, request);

        assertThat(response.getRole()).isEqualTo(Role.JUDGE);
    }

    @Test
    @DisplayName("존재하지 않는 유저 역할 변경 시 예외 발생")
    void updateRole_userNotFound() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        setField(request, "role", Role.JUDGE);

        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateRole(99L, request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
