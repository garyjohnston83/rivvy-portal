package com.rivvystudios.portal.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "brief_item_deliverable")
public class BriefItemDeliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brief_item_id", nullable = false)
    private BriefItem briefItem;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "aspect_ratio", nullable = false)
    private String aspectRatio;

    @Column(name = "max_duration_ms")
    private Integer maxDurationMs;

    @Column(name = "target_width")
    private Integer targetWidth;

    @Column(name = "target_height")
    private Integer targetHeight;

    @Column(name = "frame_rate_numerator")
    private Integer frameRateNumerator;

    @Column(name = "frame_rate_denominator")
    private Integer frameRateDenominator;

    @Column(name = "audio_required", nullable = false)
    private Boolean audioRequired;

    @Column(name = "captions_required", nullable = false)
    private Boolean captionsRequired;

    @Column(name = "localization_required", nullable = false)
    private Boolean localizationRequired;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "locales", columnDefinition = "text[]")
    private String[] locales;

    @Column(name = "deliverable_notes")
    private String deliverableNotes;

    @Type(JsonType.class)
    @Column(name = "extras", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> extras;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "locked_at", columnDefinition = "timestamptz")
    private Instant lockedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by")
    private UserAccount lockedBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BriefItem getBriefItem() {
        return briefItem;
    }

    public void setBriefItem(BriefItem briefItem) {
        this.briefItem = briefItem;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Integer getMaxDurationMs() {
        return maxDurationMs;
    }

    public void setMaxDurationMs(Integer maxDurationMs) {
        this.maxDurationMs = maxDurationMs;
    }

    public Integer getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(Integer targetWidth) {
        this.targetWidth = targetWidth;
    }

    public Integer getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(Integer targetHeight) {
        this.targetHeight = targetHeight;
    }

    public Integer getFrameRateNumerator() {
        return frameRateNumerator;
    }

    public void setFrameRateNumerator(Integer frameRateNumerator) {
        this.frameRateNumerator = frameRateNumerator;
    }

    public Integer getFrameRateDenominator() {
        return frameRateDenominator;
    }

    public void setFrameRateDenominator(Integer frameRateDenominator) {
        this.frameRateDenominator = frameRateDenominator;
    }

    public Boolean getAudioRequired() {
        return audioRequired;
    }

    public void setAudioRequired(Boolean audioRequired) {
        this.audioRequired = audioRequired;
    }

    public Boolean getCaptionsRequired() {
        return captionsRequired;
    }

    public void setCaptionsRequired(Boolean captionsRequired) {
        this.captionsRequired = captionsRequired;
    }

    public Boolean getLocalizationRequired() {
        return localizationRequired;
    }

    public void setLocalizationRequired(Boolean localizationRequired) {
        this.localizationRequired = localizationRequired;
    }

    public String[] getLocales() {
        return locales;
    }

    public void setLocales(String[] locales) {
        this.locales = locales;
    }

    public String getDeliverableNotes() {
        return deliverableNotes;
    }

    public void setDeliverableNotes(String deliverableNotes) {
        this.deliverableNotes = deliverableNotes;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    public UserAccount getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(UserAccount lockedBy) {
        this.lockedBy = lockedBy;
    }
}
