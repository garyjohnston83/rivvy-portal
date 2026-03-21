package com.rivvystudios.portal.service;

import com.rivvystudios.portal.controller.dto.BriefResponse;
import com.rivvystudios.portal.controller.dto.BriefUpdateRequest;
import com.rivvystudios.portal.model.Brief;
import com.rivvystudios.portal.model.Organization;
import com.rivvystudios.portal.model.OrganizationMember;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.BriefPriority;
import com.rivvystudios.portal.model.enums.BriefStatus;
import com.rivvystudios.portal.repository.BriefRepository;
import com.rivvystudios.portal.repository.OrganizationMemberRepository;
import com.rivvystudios.portal.repository.ProducerAssignmentRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BriefService {

    private final BriefRepository briefRepository;
    private final UserAccountRepository userAccountRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProducerAssignmentRepository producerAssignmentRepository;

    public BriefService(BriefRepository briefRepository,
                        UserAccountRepository userAccountRepository,
                        OrganizationMemberRepository organizationMemberRepository,
                        ProducerAssignmentRepository producerAssignmentRepository) {
        this.briefRepository = briefRepository;
        this.userAccountRepository = userAccountRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.producerAssignmentRepository = producerAssignmentRepository;
    }

    public Brief createDraft(String email) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserAccount(userAccount);
        if (memberships.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "User has no organization membership");
        }

        Organization org = memberships.get(0).getOrganization();

        Brief brief = new Brief();
        brief.setStatus(BriefStatus.DRAFT);
        brief.setPriority(BriefPriority.NORMAL);
        brief.setTitle("Untitled Brief");
        brief.setDescription(null);
        brief.setMetadata(Map.of());
        brief.setReferences(Map.of());
        brief.setCreatedAt(Instant.now());
        brief.setSubmittedBy(userAccount);
        brief.setOrganization(org);

        return briefRepository.save(brief);
    }

    public Brief getBriefById(UUID id, String email) {
        Brief brief = briefRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Brief not found"));

        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserAccount(userAccount);

        // Check if user is a member of the brief's organization
        boolean isOrgMember = memberships.stream()
                .anyMatch(m -> m.getOrganization().getId().equals(brief.getOrganization().getId()));

        if (isOrgMember) {
            return brief;
        }

        // Check if user is a producer assigned to the brief's organization
        boolean isAssignedProducer = memberships.stream()
                .anyMatch(m -> producerAssignmentRepository.existsByProducerMemberAndClientOrg(m, brief.getOrganization()));

        if (isAssignedProducer) {
            return brief;
        }

        throw new ResponseStatusException(FORBIDDEN, "Access denied");
    }

    public Brief updateBrief(UUID id, BriefUpdateRequest request, String email) {
        Brief brief = briefRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Brief not found"));

        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserAccount(userAccount);

        // Only client members of the brief's owning org may update
        boolean isOrgMember = memberships.stream()
                .anyMatch(m -> m.getOrganization().getId().equals(brief.getOrganization().getId()));

        if (!isOrgMember) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        // Apply non-null fields
        if (request.getTitle() != null) {
            brief.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            brief.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            brief.setPriority(BriefPriority.valueOf(request.getPriority()));
        }
        if (request.getDesiredDueDate() != null) {
            brief.setDesiredDueDate(LocalDate.parse(request.getDesiredDueDate()));
        }
        if (request.getBudget() != null) {
            brief.setBudget(request.getBudget());
        }
        if (request.getCreativeDirection() != null) {
            brief.setCreativeDirection(request.getCreativeDirection());
        }

        brief.setUpdatedAt(Instant.now());

        return briefRepository.save(brief);
    }

    public void deleteBrief(UUID id, String email) {
        Brief brief = briefRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Brief not found"));

        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserAccount(userAccount);

        // Only client members of the brief's owning org may delete
        boolean isOrgMember = memberships.stream()
                .anyMatch(m -> m.getOrganization().getId().equals(brief.getOrganization().getId()));

        if (!isOrgMember) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        if (brief.getStatus() != BriefStatus.DRAFT) {
            throw new ResponseStatusException(CONFLICT, "Only draft briefs can be deleted");
        }

        briefRepository.delete(brief);
    }

    public BriefResponse toResponse(Brief brief) {
        BriefResponse response = new BriefResponse();
        response.setId(brief.getId());
        response.setOrgId(brief.getOrganization().getId());
        response.setSubmittedById(brief.getSubmittedBy().getId());
        response.setTitle(brief.getTitle());
        response.setDescription(brief.getDescription());
        response.setStatus(brief.getStatus().name());
        response.setPriority(brief.getPriority().name());
        response.setDesiredDueDate(brief.getDesiredDueDate() != null ? brief.getDesiredDueDate().toString() : null);
        response.setBudget(brief.getBudget());
        response.setCreativeDirection(brief.getCreativeDirection());
        response.setMetadata(brief.getMetadata());
        response.setReferences(brief.getReferences());
        response.setCreatedAt(brief.getCreatedAt() != null ? brief.getCreatedAt().toString() : null);
        response.setUpdatedAt(brief.getUpdatedAt() != null ? brief.getUpdatedAt().toString() : null);
        return response;
    }
}
