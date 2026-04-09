package com.healthcarenow.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicUploadResponse {
    
    private String id;
    
    @JsonProperty("file_url")
    private String fileUrl;
    
    @JsonProperty("file_name")
    private String fileName;
    
    @JsonProperty("uploaded_at")
    private LocalDateTime uploadedAt;
    
    private Long size;
    
    private String status;
}
