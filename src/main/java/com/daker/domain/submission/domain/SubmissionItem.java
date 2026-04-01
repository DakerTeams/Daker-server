package com.daker.domain.submission.domain;

import com.daker.domain.hackathon.domain.HackathonSubmissionRule;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submission_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    // memo 항목처럼 rule이 없는 경우를 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private HackathonSubmissionRule rule;

    @Column(columnDefinition = "TEXT")
    private String valueText;

    @Column(length = 1000)
    private String valueUrl;

    // S3 object key 저장 (버킷/리전은 환경변수로 관리)
    @Column(length = 500)
    private String fileName;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 20)
    private String fileExtension;

    private Long fileSize;

    @Column(nullable = false)
    private boolean isFinal;

    @Builder
    public SubmissionItem(Submission submission, HackathonSubmissionRule rule,
                          String valueText, String valueUrl,
                          String fileName, String originalFileName, String fileExtension, Long fileSize,
                          boolean isFinal) {
        this.submission = submission;
        this.rule = rule;
        this.valueText = valueText;
        this.valueUrl = valueUrl;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.isFinal = isFinal;
    }
}
