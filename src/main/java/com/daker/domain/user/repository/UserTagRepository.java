package com.daker.domain.user.repository;

import com.daker.domain.user.domain.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {

    List<UserTag> findAllByUserId(Long userId);

    boolean existsByUserIdAndTagId(Long userId, Long tagId);

    void deleteByUserIdAndTagId(Long userId, Long tagId);

    // 유저 태그와 일치하는 hackathonId 목록 (관심 해커톤)
    @Query("SELECT DISTINCT ht.hackathon.id FROM HackathonTag ht " +
           "WHERE ht.tag.id IN " +
           "(SELECT ut.tag.id FROM UserTag ut WHERE ut.user.id = :userId) " +
           "AND ht.hackathon.deleted = false")
    List<Long> findMatchingHackathonIds(@Param("userId") Long userId);
}
