package com.daker.domain.user;

import com.daker.domain.user.domain.AccountStatus;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import com.daker.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .password("encoded_password")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("이메일로 유저를 조회한다")
    void findByEmail() {
        userRepository.save(createUser("test@test.com", "tester"));

        Optional<User> result = userRepository.findByEmail("test@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("이메일과 ACTIVE 상태로 유저를 조회한다")
    void findByEmailAndAccountStatus() {
        userRepository.save(createUser("active@test.com", "active_user"));

        Optional<User> result = userRepository.findByEmailAndAccountStatus("active@test.com", AccountStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("BANNED 상태 유저는 ACTIVE 조회에서 제외된다")
    void findByEmailAndAccountStatus_banned() {
        User user = createUser("banned@test.com", "banned_user");
        user.updateAccountStatus(AccountStatus.BANNED);
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmailAndAccountStatus("banned@test.com", AccountStatus.ACTIVE);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일 중복 여부를 확인한다")
    void existsByEmail() {
        userRepository.save(createUser("dup@test.com", "dup_user"));

        assertThat(userRepository.existsByEmail("dup@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("none@test.com")).isFalse();
    }

    @Test
    @DisplayName("닉네임 중복 여부를 확인한다")
    void existsByNickname() {
        userRepository.save(createUser("nick@test.com", "unique_nick"));

        assertThat(userRepository.existsByNickname("unique_nick")).isTrue();
        assertThat(userRepository.existsByNickname("other_nick")).isFalse();
    }
}
