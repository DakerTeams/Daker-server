package com.daker.domain.user.repository;

import com.daker.domain.user.domain.AccountStatus;
import com.daker.domain.user.domain.Role;
import com.daker.domain.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndAccountStatus(String email, AccountStatus accountStatus);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Page<User> findAllByRole(Role role, Pageable pageable);

    Page<User> findAll(Pageable pageable);

    long countByCreatedAtAfter(LocalDateTime dateTime);
}
