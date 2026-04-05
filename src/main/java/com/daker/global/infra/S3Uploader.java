package com.daker.global.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.folder}")
    private String folder;

    @Value("${cloud.aws.region}")
    private String region;

    /**
     * 파일을 S3에 업로드하고 저장 경로(key)를 반환합니다.
     * 실제 URL은 필요 시 bucket + region 조합으로 구성합니다.
     *
     * @param file      업로드할 파일
     * @param directory S3 디렉토리 경로 (예: "submissions/hackathon-1/team-1")
     * @return S3 object key (예: "daker/submissions/hackathon-1/team-1/uuid_filename.pdf")
     */
    public String upload(MultipartFile file, String directory) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String key = folder + "/" + directory + "/" + fileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드에 실패했습니다.", e);
        }

        return key;
    }

    public String getFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    public byte[] download(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        return response.asByteArray();
    }
}
