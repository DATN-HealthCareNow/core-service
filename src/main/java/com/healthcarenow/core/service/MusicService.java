package com.healthcarenow.core.service;

import com.healthcarenow.core.dto.MusicFileDTO;
import com.healthcarenow.core.dto.MusicUploadResponse;
import com.healthcarenow.core.model.mongo.MusicFile;
import com.healthcarenow.core.repository.mongo.MusicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicRepository musicRepository;

    @Value("${ACCESS_KEY}")
    private String accessKey;

    @Value("${SECRET_KEY}")
    private String secretKey;

    @Value("${AWS_REGION:ap-southeast-1}")
    private String region;

    @Value("${AWS_S3_BUCKET:healthcare-now-music}")
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

    /**
     * Upload music file to S3 and save metadata to MongoDB
     */
    public MusicUploadResponse uploadMusic(
            String userId,
            MultipartFile file,
            String fileName,
            String contentType,
            String description) {
        try {
            String resolvedFileName = resolveFileName(file, fileName);
            String resolvedContentType = resolveContentType(file, contentType);
            long fileSize = file.getSize();

            log.info("Uploading music file: {} ({} bytes) for user: {}",
                resolvedFileName, fileSize, userId);

            // Validate file size (50MB max)
            if (fileSize > 50 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds maximum limit of 50MB");
            }

            // Generate S3 key
            String s3Key = generateS3Key(userId, resolvedFileName);

            // Upload to S3
            uploadToS3(file, s3Key, resolvedContentType);

            // Generate S3 URL
            String fileUrl = generateS3Url(s3Key);

            // Save metadata to MongoDB
            MusicFile musicFile = MusicFile.builder()
                    .userId(userId)
                    .fileName(resolvedFileName)
                    .fileUrl(fileUrl)
                    .s3Key(s3Key)
                    .fileSize(fileSize)
                    .contentType(resolvedContentType)
                    .status("UPLOADED")
                    .description(description)
                    .isDefault(false)
                    .build();

            musicFile = musicRepository.save(musicFile);

            log.info("Music file uploaded successfully: {} (ID: {})", resolvedFileName, musicFile.getId());

            return MusicUploadResponse.builder()
                    .id(musicFile.getId())
                    .fileUrl(musicFile.getFileUrl())
                    .fileName(musicFile.getFileName())
                    .uploadedAt(musicFile.getUploadedAt())
                    .size(musicFile.getFileSize())
                    .status(musicFile.getStatus())
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("Invalid file: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Error reading uploaded file", e);
            throw new RuntimeException("Failed to read uploaded music file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading music file", e);
            throw new RuntimeException("Failed to upload music file: " + e.getMessage());
        }
    }

    /**
     * Get all music files for a user
     */
    public List<MusicFileDTO> getUserMusic(String userId) {
        return musicRepository.findByUserIdAndStatus(userId, "UPLOADED")
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific music file
     */
    public MusicFileDTO getMusic(String musicId, String userId) {
        MusicFile musicFile = musicRepository.findByIdAndUserId(musicId, userId)
                .orElseThrow(() -> new RuntimeException("Music file not found"));
        return convertToDTO(musicFile);
    }

    /**
     * Delete music file from both S3 and MongoDB
     */
    public void deleteMusic(String musicId, String userId) {
        MusicFile musicFile = musicRepository.findByIdAndUserId(musicId, userId)
                .orElseThrow(() -> new RuntimeException("Music file not found"));

        try {
            // Delete from S3
            deleteFromS3(musicFile.getS3Key());
            log.info("Deleted music file from S3: {}", musicFile.getS3Key());

            // Delete from MongoDB
            musicRepository.deleteByIdAndUserId(musicId, userId);
            log.info("Deleted music file from database: {}", musicId);

        } catch (Exception e) {
            log.error("Error deleting music file", e);
            throw new RuntimeException("Failed to delete music file: " + e.getMessage());
        }
    }

    /**
     * Get default/suggestion music files (âm thanh mưa, etc.)
     */
    public List<MusicFileDTO> getDefaultMusic() {
        return musicRepository.findByIsDefaultTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== Helper Methods =====

    private void uploadToS3(MultipartFile file, String s3Key, String contentType) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .acl("public-read")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    private void deleteFromS3(String s3Key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String generateS3Key(String userId, String fileName) {
        // Pattern: music/{userId}/{timestamp}_{fileName}
        long timestamp = System.currentTimeMillis();
        return String.format("music/%s/%d_%s", userId, timestamp, fileName);
    }

    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    private String resolveFileName(MultipartFile file, String fileName) {
        if (fileName != null && !fileName.isBlank()) {
            return fileName;
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null && !originalName.isBlank()) {
            return originalName;
        }

        return "audio-file.mp3";
    }

    private String resolveContentType(MultipartFile file, String contentType) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }

        if (file.getContentType() != null && !file.getContentType().isBlank()) {
            return file.getContentType();
        }

        return "audio/mpeg";
    }

    private MusicFileDTO convertToDTO(MusicFile musicFile) {
        return MusicFileDTO.builder()
                .id(musicFile.getId())
                .fileName(musicFile.getFileName())
                .fileUrl(musicFile.getFileUrl())
                .size(musicFile.getFileSize())
                .uploadedAt(musicFile.getUploadedAt())
                .status(musicFile.getStatus())
                .description(musicFile.getDescription())
                .build();
    }
}
