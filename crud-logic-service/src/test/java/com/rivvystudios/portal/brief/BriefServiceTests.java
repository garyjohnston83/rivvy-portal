package com.rivvystudios.portal.brief;

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
import com.rivvystudios.portal.service.BriefService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BriefServiceTests {

    @Mock
    private BriefRepository briefRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private ProducerAssignmentRepository producerAssignmentRepository;

    @InjectMocks
    private BriefService briefService;

    @Test
    void createDraft_setsDefaultValues() {
        UserAccount user = buildUser();
        Organization org = buildOrg();
        OrganizationMember membership = buildMembership(user, org);

        when(userAccountRepository.findByEmail("client@test.com")).thenReturn(Optional.of(user));
        when(organizationMemberRepository.findByUserAccount(user)).thenReturn(List.of(membership));
        when(briefRepository.save(any(Brief.class))).thenAnswer(invocation -> {
            Brief b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        Brief result = briefService.createDraft("client@test.com");

        assertThat(result.getStatus()).isEqualTo(BriefStatus.DRAFT);
        assertThat(result.getPriority()).isEqualTo(BriefPriority.NORMAL);
        assertThat(result.getTitle()).isEqualTo("Untitled Brief");
        assertThat(result.getMetadata()).isEqualTo(Map.of());
        assertThat(result.getReferences()).isEqualTo(Map.of());
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void createDraft_resolvesOrgFromUserMembership() {
        UserAccount user = buildUser();
        Organization org = buildOrg();
        OrganizationMember membership = buildMembership(user, org);

        when(userAccountRepository.findByEmail("client@test.com")).thenReturn(Optional.of(user));
        when(organizationMemberRepository.findByUserAccount(user)).thenReturn(List.of(membership));
        when(briefRepository.save(any(Brief.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Brief result = briefService.createDraft("client@test.com");

        assertThat(result.getOrganization()).isEqualTo(org);
        assertThat(result.getSubmittedBy()).isEqualTo(user);
    }

    @Test
    void updateBrief_appliesPartialFieldUpdates() {
        UserAccount user = buildUser();
        Organization org = buildOrg();
        OrganizationMember membership = buildMembership(user, org);

        Brief brief = buildBrief(org, user);
        brief.setTitle("Old Title");

        when(briefRepository.findById(brief.getId())).thenReturn(Optional.of(brief));
        when(userAccountRepository.findByEmail("client@test.com")).thenReturn(Optional.of(user));
        when(organizationMemberRepository.findByUserAccount(user)).thenReturn(List.of(membership));
        when(briefRepository.save(any(Brief.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BriefUpdateRequest request = new BriefUpdateRequest();
        request.setTitle("New Title");
        // description, priority, etc. are null -- should not be applied

        Brief result = briefService.updateBrief(brief.getId(), request, "client@test.com");

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getDescription()).isNull(); // was null before, stays null
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateBrief_producerGetsForbidden() {
        UserAccount producerUser = buildUser();
        producerUser.setId(UUID.randomUUID());
        producerUser.setEmail("producer@test.com");

        Organization producerOrg = new Organization();
        producerOrg.setId(UUID.randomUUID());
        producerOrg.setName("Producer Org");

        OrganizationMember producerMembership = buildMembership(producerUser, producerOrg);

        Organization clientOrg = buildOrg();
        UserAccount clientUser = buildUser();
        Brief brief = buildBrief(clientOrg, clientUser);

        when(briefRepository.findById(brief.getId())).thenReturn(Optional.of(brief));
        when(userAccountRepository.findByEmail("producer@test.com")).thenReturn(Optional.of(producerUser));
        when(organizationMemberRepository.findByUserAccount(producerUser)).thenReturn(List.of(producerMembership));

        BriefUpdateRequest request = new BriefUpdateRequest();
        request.setTitle("Hacked Title");

        assertThatThrownBy(() -> briefService.updateBrief(brief.getId(), request, "producer@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void deleteBrief_onlyAllowedForDraftStatus() {
        UserAccount user = buildUser();
        Organization org = buildOrg();
        OrganizationMember membership = buildMembership(user, org);

        Brief draftBrief = buildBrief(org, user);
        draftBrief.setStatus(BriefStatus.DRAFT);

        Brief submittedBrief = buildBrief(org, user);
        submittedBrief.setId(UUID.randomUUID());
        submittedBrief.setStatus(BriefStatus.SUBMITTED);

        // Draft deletion should succeed
        when(briefRepository.findById(draftBrief.getId())).thenReturn(Optional.of(draftBrief));
        when(userAccountRepository.findByEmail("client@test.com")).thenReturn(Optional.of(user));
        when(organizationMemberRepository.findByUserAccount(user)).thenReturn(List.of(membership));

        briefService.deleteBrief(draftBrief.getId(), "client@test.com");
        verify(briefRepository).delete(draftBrief);

        // Submitted deletion should fail with 409
        when(briefRepository.findById(submittedBrief.getId())).thenReturn(Optional.of(submittedBrief));

        assertThatThrownBy(() -> briefService.deleteBrief(submittedBrief.getId(), "client@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void getBrief_producerWithAssignmentCanView() {
        UserAccount producerUser = buildUser();
        producerUser.setId(UUID.randomUUID());
        producerUser.setEmail("producer@test.com");

        Organization producerOrg = new Organization();
        producerOrg.setId(UUID.randomUUID());
        producerOrg.setName("Producer Org");

        OrganizationMember producerMembership = buildMembership(producerUser, producerOrg);

        Organization clientOrg = buildOrg();
        UserAccount clientUser = buildUser();
        Brief brief = buildBrief(clientOrg, clientUser);

        when(briefRepository.findById(brief.getId())).thenReturn(Optional.of(brief));
        when(userAccountRepository.findByEmail("producer@test.com")).thenReturn(Optional.of(producerUser));
        when(organizationMemberRepository.findByUserAccount(producerUser)).thenReturn(List.of(producerMembership));
        when(producerAssignmentRepository.existsByProducerMemberAndClientOrg(producerMembership, clientOrg))
                .thenReturn(true);

        Brief result = briefService.getBriefById(brief.getId(), "producer@test.com");

        assertThat(result.getId()).isEqualTo(brief.getId());
    }

    // --- Helper methods ---

    private UserAccount buildUser() {
        UserAccount user = new UserAccount();
        user.setId(UUID.fromString("30000000-0000-0000-0000-000000000001"));
        user.setEmail("client@test.com");
        user.setFirstName("Test");
        user.setLastName("Client");
        return user;
    }

    private Organization buildOrg() {
        Organization org = new Organization();
        org.setId(UUID.fromString("20000000-0000-0000-0000-000000000001"));
        org.setName("Test Org");
        return org;
    }

    private OrganizationMember buildMembership(UserAccount user, Organization org) {
        OrganizationMember membership = new OrganizationMember();
        membership.setId(UUID.randomUUID());
        membership.setUserAccount(user);
        membership.setOrganization(org);
        membership.setIsPrimary(true);
        membership.setJoinedAt(Instant.now());
        return membership;
    }

    private Brief buildBrief(Organization org, UserAccount user) {
        Brief brief = new Brief();
        brief.setId(UUID.randomUUID());
        brief.setOrganization(org);
        brief.setSubmittedBy(user);
        brief.setTitle("Test Brief");
        brief.setStatus(BriefStatus.DRAFT);
        brief.setPriority(BriefPriority.NORMAL);
        brief.setMetadata(Map.of());
        brief.setReferences(Map.of());
        brief.setCreatedAt(Instant.now());
        return brief;
    }
}
