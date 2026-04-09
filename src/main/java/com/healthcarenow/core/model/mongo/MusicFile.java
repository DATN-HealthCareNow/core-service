package com.healthcarenow.core.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "music_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicFile {

    @Id
    private String id;

    private String userId;

    private String fileName;

    private String fileUrl; // S3 URL

    private String s3Key; // S3 Key for deletion/management

    private Long fileSize; // Bytes

    private String contentType; // audio/mpeg, audio/wav, etc.

    private String status; // UPLOADED, DELETED, PROCESSING

    @CreatedDate
    private LocalDateTime uploadedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String description; // Optional metadata

    private Boolean isDefault; // For default music suggestions
}
