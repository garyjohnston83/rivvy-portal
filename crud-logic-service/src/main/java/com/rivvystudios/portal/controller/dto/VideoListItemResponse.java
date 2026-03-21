package com.rivvystudios.portal.controller.dto;

import java.util.UUID;

public class VideoListItemResponse {

    private UUID id;
    private String title;
    private Integer currentVersionNumber;
    private boolean isApproved;

    public VideoListItemResponse() {
    }

    public VideoListItemResponse(UUID id, String title, Integer currentVersionNumber, boolean isApproved) {
        this.id = id;
        this.title = title;
        this.currentVersionNumber = currentVersionNumber;
        this.isApproved = isApproved;
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
}
