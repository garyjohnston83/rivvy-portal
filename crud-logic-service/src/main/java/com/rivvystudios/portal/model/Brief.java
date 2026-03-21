package com.rivvystudios.portal.model;

import com.rivvystudios.portal.model.enums.BriefPriority;
import com.rivvystudios.portal.model.enums.BriefStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "brief")
public class Brief {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private UserAccount submittedBy;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BriefStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private BriefPriority priority;

    @Column(name = "desired_due_date")
    private LocalDate desiredDueDate;

    @Column(name = "budget", precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "creative_direction")
    private String creativeDirection;

    @Type(JsonType.class)
    @Column(name = "\"references\"", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> references;

    @Type(JsonType.class)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;

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

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public UserAccount getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(UserAccount submittedBy) {
        this.submittedBy = submittedBy;
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

    public BriefStatus getStatus() {
        return status;
    }

    public void setStatus(BriefStatus status) {
        this.status = status;
    }

    public BriefPriority getPriority() {
        return priority;
    }

    public void setPriority(BriefPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDesiredDueDate() {
        return desiredDueDate;
    }

    public void setDesiredDueDate(LocalDate desiredDueDate) {
        this.desiredDueDate = desiredDueDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getCreativeDirection() {
        return creativeDirection;
    }

    public void setCreativeDirection(String creativeDirection) {
        this.creativeDirection = creativeDirection;
    }

    public Map<String, Object> getReferences() {
        return references;
    }

    public void setReferences(Map<String, Object> references) {
        this.references = references;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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
