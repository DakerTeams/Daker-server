package com.daker.domain.submission.dto;

import lombok.Getter;

@Getter
public class DownloadFilePayload {

    private final byte[] bytes;
    private final String fileName;
    private final String contentType;

    public DownloadFilePayload(byte[] bytes, String fileName, String contentType) {
        this.bytes = bytes;
        this.fileName = fileName;
        this.contentType = contentType;
    }
}
