package com.healthcarenow.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

  @Value("${ACCESS_KEY}")
  private String accessKey;

  @Value("${SECRET_KEY}")
  private String secretKey;

  @Value("${AWS_REGION:ap-southeast-1}")
  private String region;

  @Value("${AWS_S3_BUCKET:healthcare-now-avatars}")
  private String bucketName;

  private S3Client s3Client;

  @PostConstruct
  public void init() {
    this.s3Client = S3Client.builder()
        .region(Region.of(region.trim()))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())))
        .build();
  }

  public String uploadFile(MultipartFile file, String folder) throws IOException {
    String fileName = folder + "/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .contentType(file.getContentType())
        .acl("public-read")
        .build();

    s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
  }
}
