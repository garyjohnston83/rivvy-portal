package com.rivvystudios.portal.model;

import com.rivvystudios.portal.model.enums.TranscodeStatus;
import com.rivvystudios.portal.model.enums.UploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "video_version")
public class VideoVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_object_id", nullable = false)
    private StorageObject storageObject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserAccount uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private UploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "transcode_status", nullable = false)
    private TranscodeStatus transcodeStatus;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public StorageObject getStorageObject() {
        return storageObject;
    }

    public void setStorageObject(StorageObject storageObject) {
        this.storageObject = storageObject;
    }

    public UserAccount getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(UserAccount uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public TranscodeStatus getTranscodeStatus() {
        return transcodeStatus;
    }

    public void setTranscodeStatus(TranscodeStatus transcodeStatus) {
        this.transcodeStatus = transcodeStatus;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
