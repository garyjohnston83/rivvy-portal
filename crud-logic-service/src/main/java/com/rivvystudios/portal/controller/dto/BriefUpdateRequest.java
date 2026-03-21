package com.rivvystudios.portal.controller.dto;

import java.math.BigDecimal;

public class BriefUpdateRequest {

    private String title;
    private String description;
    private String priority;
    private String desiredDueDate;
    private BigDecimal budget;
    private String creativeDirection;

    public BriefUpdateRequest() {
    }

    public BriefUpdateRequest(String title, String description, String priority,
                              String desiredDueDate, BigDecimal budget, String creativeDirection) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.desiredDueDate = desiredDueDate;
        this.budget = budget;
        this.creativeDirection = creativeDirection;
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
}
