package com.rivvystudios.portal.controller;

import com.rivvystudios.portal.controller.dto.BriefResponse;
import com.rivvystudios.portal.controller.dto.BriefUpdateRequest;
import com.rivvystudios.portal.model.Brief;
import com.rivvystudios.portal.service.BriefService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/briefs")
public class BriefController {

    private final BriefService briefService;

    public BriefController(BriefService briefService) {
        this.briefService = briefService;
    }

    @PostMapping
    public ResponseEntity<BriefResponse> createDraft() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Brief brief = briefService.createDraft(email);
        BriefResponse response = briefService.toResponse(brief);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BriefResponse> getBrief(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Brief brief = briefService.getBriefById(id, email);
        return ResponseEntity.ok(briefService.toResponse(brief));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BriefResponse> updateBrief(@PathVariable UUID id,
                                                     @RequestBody BriefUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Brief updatedBrief = briefService.updateBrief(id, request, email);
        return ResponseEntity.ok(briefService.toResponse(updatedBrief));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrief(@PathVariable UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        briefService.deleteBrief(id, email);
        return ResponseEntity.noContent().build();
    }
}
