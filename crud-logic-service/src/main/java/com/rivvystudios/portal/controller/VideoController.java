package com.rivvystudios.portal.controller;

import com.rivvystudios.portal.controller.dto.VideoDetailResponse;
import com.rivvystudios.portal.controller.dto.VideoListItemResponse;
import com.rivvystudios.portal.service.VideoService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public ResponseEntity<Page<VideoListItemResponse>> getVideosByProject(
            @RequestParam UUID projectId,
            @RequestParam(defaultValue = "0") int page) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(videoService.getVideosByProject(email, projectId, page));
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoDetailResponse> getVideoDetail(@PathVariable UUID videoId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(videoService.getVideoDetail(email, videoId));
    }
}
