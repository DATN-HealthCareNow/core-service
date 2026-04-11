package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.MusicFileDTO;
import com.healthcarenow.core.dto.MusicUploadResponse;
import com.healthcarenow.core.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/music")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class MusicController {

    private final MusicService musicService;

    /**
     * Upload music file
     * POST /api/v1/music/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MusicUploadResponse> uploadMusic(
            @AuthenticationPrincipal String userId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "contentType", required = false) String contentType,
            @RequestParam(value = "description", required = false) String description) {
        
        log.info("Uploading music file: {} for user: {}", fileName, userId);
        
        MusicUploadResponse response = musicService.uploadMusic(userId, file, fileName, contentType, description);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all music files for current user
     * GET /api/v1/music/my-music
     */
    @GetMapping("/my-music")
    public ResponseEntity<List<MusicFileDTO>> getMyMusic(
            @AuthenticationPrincipal String userId) {
        
        log.info("Fetching music files for user: {}", userId);
        
        List<MusicFileDTO> musicList = musicService.getUserMusic(userId);
        
        return ResponseEntity.ok(musicList);
    }

    /**
     * Get a specific music file
     * GET /api/v1/music/{musicId}
     */
    @GetMapping("/{musicId}")
    public ResponseEntity<MusicFileDTO> getMusic(
            @PathVariable String musicId,
            @AuthenticationPrincipal String userId) {
        
        log.info("Fetching music file: {} for user: {}", musicId, userId);
        
        MusicFileDTO music = musicService.getMusic(musicId, userId);
        
        return ResponseEntity.ok(music);
    }

    /**
     * Delete music file
     * DELETE /api/v1/music/{musicId}
     */
    @DeleteMapping("/{musicId}")
    public ResponseEntity<Void> deleteMusic(
            @PathVariable String musicId,
            @AuthenticationPrincipal String userId) {
        
        log.info("Deleting music file: {} for user: {}", musicId, userId);
        
        musicService.deleteMusic(musicId, userId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get default/suggestion music (e.g., ambient sounds)
     * GET /api/v1/music/default/suggestions
     */
    @GetMapping("/default/suggestions")
    public ResponseEntity<List<MusicFileDTO>> getDefaultMusic() {
        
        log.info("Fetching default music suggestions");
        
        List<MusicFileDTO> defaultMusic = musicService.getDefaultMusic();
        
        return ResponseEntity.ok(defaultMusic);
    }

    /**
     * Health check endpoint
     * GET /api/v1/music/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Music service is running");
    }
}
