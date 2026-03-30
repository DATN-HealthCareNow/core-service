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
public class MusicFileDTO {
    
    private String id;
    
    @JsonProperty("file_name")
    private String fileName;
    
    @JsonProperty("file_url")
    private String fileUrl;
    
    private Long size;
    
    @JsonProperty("uploaded_at")
    private LocalDateTime uploadedAt;
    
    private String status;
    
    private String description;
}
