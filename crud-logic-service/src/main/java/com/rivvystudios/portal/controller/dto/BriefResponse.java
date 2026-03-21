package com.rivvystudios.portal.controller.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class BriefResponse {

    private UUID id;
    private UUID orgId;
    private UUID submittedById;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String desiredDueDate;
    private BigDecimal budget;
    private String creativeDirection;
    private Map<String, Object> metadata;
    private Map<String, Object> references;
    private String createdAt;
    private String updatedAt;

    public BriefResponse() {
    }

    public BriefResponse(UUID id, UUID orgId, UUID submittedById, String title, String description,
                         String status, String priority, String desiredDueDate, BigDecimal budget,
                         String creativeDirection, Map<String, Object> metadata, Map<String, Object> references,
                         String createdAt, String updatedAt) {
        this.id = id;
        this.orgId = orgId;
        this.submittedById = submittedById;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.desiredDueDate = desiredDueDate;
        this.budget = budget;
        this.creativeDirection = creativeDirection;
        this.metadata = metadata;
        this.references = references;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public UUID getSubmittedById() {
        return submittedById;
    }

    public void setSubmittedById(UUID submittedById) {
        this.submittedById = submittedById;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDesiredDueDate() {
        return desiredDueDate;
    }

    public void setDesiredDueDate(String desiredDueDate) {
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getReferences() {
        return references;
    }

    public void setReferences(Map<String, Object> references) {
        this.references = references;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
