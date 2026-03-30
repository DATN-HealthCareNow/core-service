package com.healthcarenow.core.controller;

import com.healthcarenow.core.dto.MusicFileDTO;
import com.healthcarenow.core.dto.MusicUploadRequest;
import com.healthcarenow.core.dto.MusicUploadResponse;
import com.healthcarenow.core.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
    @PostMapping("/upload")
    public ResponseEntity<MusicUploadResponse> uploadMusic(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody MusicUploadRequest request) {
        
        log.info("Uploading music file: {} for user: {}", request.getFileName(), userId);
        
        MusicUploadResponse response = musicService.uploadMusic(userId, request);
        
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
