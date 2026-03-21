package com.rivvystudios.portal.service;

import com.rivvystudios.portal.controller.dto.BrandAssetCountsResponse;
import com.rivvystudios.portal.model.Organization;
import com.rivvystudios.portal.model.OrganizationMember;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.BrandAssetType;
import com.rivvystudios.portal.repository.BrandAssetRepository;
import com.rivvystudios.portal.repository.OrganizationMemberRepository;
import com.rivvystudios.portal.repository.ProducerAssignmentRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BrandAssetService {

    private final BrandAssetRepository brandAssetRepository;
    private final UserAccountRepository userAccountRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProducerAssignmentRepository producerAssignmentRepository;

    public BrandAssetService(BrandAssetRepository brandAssetRepository,
                             UserAccountRepository userAccountRepository,
                             OrganizationMemberRepository organizationMemberRepository,
                             ProducerAssignmentRepository producerAssignmentRepository) {
        this.brandAssetRepository = brandAssetRepository;
        this.userAccountRepository = userAccountRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.producerAssignmentRepository = producerAssignmentRepository;
    }

    public BrandAssetCountsResponse getCategoryCounts(String email, UUID projectId) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        List<OrganizationMember> memberships = organizationMemberRepository.findByUserAccount(userAccount);
        if (memberships.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "User has no organization membership");
        }

        Organization org = memberships.get(0).getOrganization();

        // Check if user is a member of the organization
        boolean isOrgMember = memberships.stream()
                .anyMatch(m -> m.getOrganization().getId().equals(org.getId()));

        if (!isOrgMember) {
            // Check if user is a producer assigned to the organization
            boolean isAssignedProducer = memberships.stream()
                    .anyMatch(m -> producerAssignmentRepository.existsByProducerMemberAndClientOrg(m, org));

            if (!isAssignedProducer) {
                throw new ResponseStatusException(FORBIDDEN, "Access denied");
            }
        }

        // Get org-scoped counts
        List<Object[]> orgResults = brandAssetRepository.countByOrgGroupedByType(org.getId());
        Map<String, Long> orgCounts = buildCountsMap(orgResults);

        // Get project-scoped counts if projectId is provided
        Map<String, Long> projectCounts = null;
        if (projectId != null) {
            List<Object[]> projectResults = brandAssetRepository.countByOrgAndProjectGroupedByType(org.getId(), projectId);
            projectCounts = buildCountsMap(projectResults);
        }

        return new BrandAssetCountsResponse(orgCounts, projectCounts);
    }

    private Map<String, Long> buildCountsMap(List<Object[]> results) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("logos", 0L);
        counts.put("fonts", 0L);
        counts.put("guidelines", 0L);
        counts.put("visuals", 0L);

        for (Object[] row : results) {
            BrandAssetType type = (BrandAssetType) row[0];
            Long count = (Long) row[1];
            counts.put(type.name().toLowerCase(), count);
        }

        return counts;
    }
}
