package com.daker.domain.submission.dto;

import com.daker.domain.submission.domain.Submission;
import com.daker.domain.submission.domain.SubmissionItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SubmissionHistoryResponse {

    private final Long submissionId;
    private final int revisionNo;
    private final boolean latest;
    private final LocalDateTime submittedAt;
    private final String memo;
    private final List<FileItem> files;

    public SubmissionHistoryResponse(Submission submission, List<SubmissionItem> items) {
        this.submissionId = submission.getId();
        this.revisionNo = submission.getRevisionNo();
        this.latest = submission.isLatest();
        this.submittedAt = submission.getSubmittedAt();
        this.memo = items.stream()
                .filter(i -> i.getValueText() != null)
                .map(SubmissionItem::getValueText)
                .findFirst()
                .orElse(null);
        this.files = items.stream()
                .filter(i -> i.getFileName() != null)
                .map(FileItem::new)
                .toList();
    }

    @Getter
    public static class FileItem {
        private final String fileName;
        private final String originalFileName;
        private final String fileExtension;
        private final Long fileSize;

        public FileItem(SubmissionItem item) {
            this.fileName = item.getFileName();
            this.originalFileName = item.getOriginalFileName();
            this.fileExtension = item.getFileExtension();
            this.fileSize = item.getFileSize();
        }
    }
}
