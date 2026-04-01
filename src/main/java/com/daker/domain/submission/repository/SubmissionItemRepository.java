package com.daker.domain.submission.repository;

import com.daker.domain.submission.domain.SubmissionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionItemRepository extends JpaRepository<SubmissionItem, Long> {

    List<SubmissionItem> findAllBySubmissionId(Long submissionId);

    void deleteAllBySubmissionId(Long submissionId);
}
