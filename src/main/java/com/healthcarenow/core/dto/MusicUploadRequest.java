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
public class MusicUploadRequest {
    
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("content_type")
    private String contentType; // e.g., "audio/mpeg"
    
    private String description;
}
