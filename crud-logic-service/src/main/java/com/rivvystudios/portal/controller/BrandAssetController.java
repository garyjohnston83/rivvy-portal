package com.rivvystudios.portal.controller;

import com.rivvystudios.portal.controller.dto.BrandAssetCountsResponse;
import com.rivvystudios.portal.service.BrandAssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/brand-assets")
public class BrandAssetController {

    private final BrandAssetService brandAssetService;

    public BrandAssetController(BrandAssetService brandAssetService) {
        this.brandAssetService = brandAssetService;
    }

    @GetMapping("/counts")
    public ResponseEntity<BrandAssetCountsResponse> getCounts(
            @RequestParam(required = false) UUID projectId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        BrandAssetCountsResponse result = brandAssetService.getCategoryCounts(email, projectId);
        return ResponseEntity.ok(result);
    }
}
