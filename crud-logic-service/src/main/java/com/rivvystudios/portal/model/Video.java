package com.rivvystudios.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "video")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brief_item_deliverable_id", nullable = false)
    private BriefItemDeliverable briefItemDeliverable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brief_item_id")
    private BriefItem briefItem;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "latest_approved_version_id")
    private UUID latestApprovedVersionId;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BriefItemDeliverable getBriefItemDeliverable() {
        return briefItemDeliverable;
    }

    public void setBriefItemDeliverable(BriefItemDeliverable briefItemDeliverable) {
        this.briefItemDeliverable = briefItemDeliverable;
    }

    public BriefItem getBriefItem() {
        return briefItem;
    }

    public void setBriefItem(BriefItem briefItem) {
        this.briefItem = briefItem;
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

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public UUID getLatestApprovedVersionId() {
        return latestApprovedVersionId;
    }

    public void setLatestApprovedVersionId(UUID latestApprovedVersionId) {
        this.latestApprovedVersionId = latestApprovedVersionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
