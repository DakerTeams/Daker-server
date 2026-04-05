package com.daker.domain.judge.dto;

import com.daker.domain.submission.domain.Submission;
import com.daker.domain.submission.domain.SubmissionItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class JudgeSubmissionResponse {

    private final Long submissionId;
    private final Long teamId;
    private final String teamName;
    private final LocalDateTime submittedAt;
    private final int revisionNo;
    private final List<ItemInfo> items;

    public JudgeSubmissionResponse(Submission submission, List<SubmissionItem> items,
                                   java.util.function.Function<String, String> keyToUrl) {
        this.submissionId = submission.getId();
        this.teamId = submission.getTeam().getId();
        this.teamName = submission.getTeam().getName();
        this.submittedAt = submission.getSubmittedAt();
        this.revisionNo = submission.getRevisionNo();
        this.items = items.stream().map(item -> new ItemInfo(item, keyToUrl)).toList();
    }

    @Getter
    public static class ItemInfo {
        private final Long itemId;
        private final String originalFileName;
        private final String fileUrl;
        private final String valueUrl;
        private final String valueText;
        private final Long fileSize;

        public ItemInfo(SubmissionItem item, java.util.function.Function<String, String> keyToUrl) {
            this.itemId = item.getId();
            this.originalFileName = item.getOriginalFileName();
            this.fileUrl = item.getFileName() != null ? keyToUrl.apply(item.getFileName()) : null;
            this.valueUrl = item.getValueUrl();
            this.valueText = item.getValueText();
            this.fileSize = item.getFileSize();
        }
    }
}
