package com.daker.domain.user.repository;

import com.daker.domain.user.domain.AccountStatus;
import com.daker.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndAccountStatus(String email, AccountStatus accountStatus);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
