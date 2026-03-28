package com.daker.domain.hackathon.repository;

import com.daker.domain.hackathon.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findAllByNameIn(List<String> names);
}
