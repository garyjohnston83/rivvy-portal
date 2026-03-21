package com.rivvystudios.portal.controller.dto;

import java.util.UUID;

public class VideoDetailResponse {

    private UUID id;
    private String title;
    private String description;
    private Integer currentVersionNumber;
    private boolean isApproved;
    private String transcodeStatus;
    private String playbackUrl;
    private String createdAt;

    public VideoDetailResponse() {
    }

    public VideoDetailResponse(UUID id, String title, String description, Integer currentVersionNumber,
                               boolean isApproved, String transcodeStatus, String playbackUrl, String createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.currentVersionNumber = currentVersionNumber;
        this.isApproved = isApproved;
        this.transcodeStatus = transcodeStatus;
        this.playbackUrl = playbackUrl;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCurrentVersionNumber() {
        return currentVersionNumber;
    }

    public void setCurrentVersionNumber(Integer currentVersionNumber) {
        this.currentVersionNumber = currentVersionNumber;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getTranscodeStatus() {
        return transcodeStatus;
    }

    public void setTranscodeStatus(String transcodeStatus) {
        this.transcodeStatus = transcodeStatus;
    }

    public String getPlaybackUrl() {
        return playbackUrl;
    }

    public void setPlaybackUrl(String playbackUrl) {
        this.playbackUrl = playbackUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
