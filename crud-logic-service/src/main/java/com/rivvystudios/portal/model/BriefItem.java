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

import java.util.UUID;

@Entity
@Table(name = "brief_item")
public class BriefItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brief_id", nullable = false)
    private Brief brief;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "expected_duration_ms")
    private Integer expectedDurationMs;

    @Column(name = "primary_aspect_ratio")
    private String primaryAspectRatio;

    @Column(name = "audience")
    private String audience;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Brief getBrief() {
        return brief;
    }

    public void setBrief(Brief brief) {
        this.brief = brief;
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

    public Integer getExpectedDurationMs() {
        return expectedDurationMs;
    }

    public void setExpectedDurationMs(Integer expectedDurationMs) {
        this.expectedDurationMs = expectedDurationMs;
    }

    public String getPrimaryAspectRatio() {
        return primaryAspectRatio;
    }

    public void setPrimaryAspectRatio(String primaryAspectRatio) {
        this.primaryAspectRatio = primaryAspectRatio;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
