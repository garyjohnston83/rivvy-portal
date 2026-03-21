package com.rivvystudios.portal.controller.dto;

import java.util.Map;

public class BrandAssetCountsResponse {

    private Map<String, Long> orgCounts;
    private Map<String, Long> projectCounts;

    public BrandAssetCountsResponse() {
    }

    public BrandAssetCountsResponse(Map<String, Long> orgCounts, Map<String, Long> projectCounts) {
        this.orgCounts = orgCounts;
        this.projectCounts = projectCounts;
    }

    public Map<String, Long> getOrgCounts() {
        return orgCounts;
    }

    public void setOrgCounts(Map<String, Long> orgCounts) {
        this.orgCounts = orgCounts;
    }

    public Map<String, Long> getProjectCounts() {
        return projectCounts;
    }

    public void setProjectCounts(Map<String, Long> projectCounts) {
        this.projectCounts = projectCounts;
    }
}
