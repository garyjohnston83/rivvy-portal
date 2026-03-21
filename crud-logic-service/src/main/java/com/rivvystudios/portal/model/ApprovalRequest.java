package com.rivvystudios.portal.model;

import com.rivvystudios.portal.model.enums.ApprovalRequestStatus;
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
@Table(name = "approval_request")
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_version_id", nullable = false)
    private VideoVersion videoVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_assignment_id", nullable = false)
    private ProducerAssignment producerAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_member_id", nullable = false)
    private OrganizationMember approverMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private UserAccount requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalRequestStatus status;

    @Column(name = "decision_note")
    private String decisionNote;

    @Column(name = "decided_at", columnDefinition = "timestamptz")
    private Instant decidedAt;

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

    public VideoVersion getVideoVersion() {
        return videoVersion;
    }

    public void setVideoVersion(VideoVersion videoVersion) {
        this.videoVersion = videoVersion;
    }

    public ProducerAssignment getProducerAssignment() {
        return producerAssignment;
    }

    public void setProducerAssignment(ProducerAssignment producerAssignment) {
        this.producerAssignment = producerAssignment;
    }

    public OrganizationMember getApproverMember() {
        return approverMember;
    }

    public void setApproverMember(OrganizationMember approverMember) {
        this.approverMember = approverMember;
    }

    public UserAccount getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(UserAccount requestedBy) {
        this.requestedBy = requestedBy;
    }

    public ApprovalRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalRequestStatus status) {
        this.status = status;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
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
