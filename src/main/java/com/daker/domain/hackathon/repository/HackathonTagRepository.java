package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.HackathonTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HackathonTagRepository extends JpaRepository<HackathonTag, Long> {

    List<HackathonTag> findAllByHackathonId(Long hackathonId);

    void deleteAllByHackathonId(Long hackathonId);

    // 태그 필터: 특정 태그를 가진 hackathonId 목록
    List<HackathonTag> findAllByTagName(String tagName);
}
