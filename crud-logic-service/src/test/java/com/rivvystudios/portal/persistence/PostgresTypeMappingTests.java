package com.rivvystudios.portal.persistence;

import com.rivvystudios.portal.TestcontainersConfiguration;
import com.rivvystudios.portal.model.Brief;
import com.rivvystudios.portal.model.BriefItem;
import com.rivvystudios.portal.model.BriefItemDeliverable;
import com.rivvystudios.portal.model.Organization;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.BriefPriority;
import com.rivvystudios.portal.model.enums.BriefStatus;
import com.rivvystudios.portal.model.enums.OrganizationStatus;
import com.rivvystudios.portal.model.enums.UserAccountStatus;
import com.rivvystudios.portal.repository.BriefItemDeliverableRepository;
import com.rivvystudios.portal.repository.BriefItemRepository;
import com.rivvystudios.portal.repository.BriefRepository;
import com.rivvystudios.portal.repository.OrganizationRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class PostgresTypeMappingTests {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BriefRepository briefRepository;

    @Autowired
    private BriefItemRepository briefItemRepository;

    @Autowired
    private BriefItemDeliverableRepository briefItemDeliverableRepository;

    @Autowired
    private EntityManager entityManager;

    private Organization createTestOrganization() {
        Organization org = new Organization();
        org.setName("Type Mapping Test Org");
        org.setSlug("type-mapping-test-org");
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());
        return organizationRepository.save(org);
    }

    private UserAccount createTestUserAccount(Organization org) {
        UserAccount user = new UserAccount();
        user.setEmail("typemapping-" + System.nanoTime() + "@example.com");
        user.setFirstName("Type");
        user.setLastName("Test");
        user.setAuthProvider("LOCAL");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());
        return userAccountRepository.save(user);
    }

    @Test
    void citextStoresMixedCaseEmail() {
        Organization org = createTestOrganization();

        UserAccount user = new UserAccount();
        user.setEmail("Test@Example.COM");
        user.setFirstName("Citext");
        user.setLastName("Test");
        user.setAuthProvider("LOCAL");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());

        UserAccount saved = userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAccount> found = userAccountRepository.findById(saved.getId());
        assertThat(found).isPresent();
        // CITEXT stores the value as-is but compares case-insensitively
        assertThat(found.get().getEmail()).isEqualTo("Test@Example.COM");
    }

    @Test
    void jsonbRoundTripsMapData() {
        Organization org = createTestOrganization();
        UserAccount user = createTestUserAccount(org);

        Map<String, Object> referencesMap = Map.of(
                "url", "https://example.com/ref",
                "notes", "Some reference notes",
                "tags", List.of("tag1", "tag2")
        );
        Map<String, Object> metadataMap = Map.of(
                "version", 1,
                "source", "test",
                "nested", Map.of("key", "value")
        );

        Brief brief = new Brief();
        brief.setOrganization(org);
        brief.setSubmittedBy(user);
        brief.setTitle("JSONB Test Brief");
        brief.setStatus(BriefStatus.DRAFT);
        brief.setPriority(BriefPriority.MEDIUM);
        brief.setReferences(referencesMap);
        brief.setMetadata(metadataMap);
        brief.setCreatedAt(Instant.now());

        Brief saved = briefRepository.save(brief);
        entityManager.flush();
        entityManager.clear();

        Optional<Brief> found = briefRepository.findById(saved.getId());
        assertThat(found).isPresent();

        Map<String, Object> retrievedRefs = found.get().getReferences();
        assertThat(retrievedRefs).containsEntry("url", "https://example.com/ref");
        assertThat(retrievedRefs).containsEntry("notes", "Some reference notes");
        assertThat(retrievedRefs).containsKey("tags");

        Map<String, Object> retrievedMeta = found.get().getMetadata();
        assertThat(retrievedMeta).containsEntry("source", "test");
        assertThat(retrievedMeta).containsKey("nested");
    }

    @Test
    void textArrayRoundTripsStringArray() {
        Organization org = createTestOrganization();
        UserAccount user = createTestUserAccount(org);

        // Create Brief (parent of BriefItem)
        Brief brief = new Brief();
        brief.setOrganization(org);
        brief.setSubmittedBy(user);
        brief.setTitle("Array Test Brief");
        brief.setStatus(BriefStatus.DRAFT);
        brief.setPriority(BriefPriority.LOW);
        brief.setReferences(Map.of());
        brief.setMetadata(Map.of());
        brief.setCreatedAt(Instant.now());
        brief = briefRepository.save(brief);

        // Create BriefItem (parent of BriefItemDeliverable)
        BriefItem briefItem = new BriefItem();
        briefItem.setBrief(brief);
        briefItem.setTitle("Array Test Item");
        briefItem.setOrderIndex(1);
        briefItem = briefItemRepository.save(briefItem);

        // Create BriefItemDeliverable with locales array
        BriefItemDeliverable deliverable = new BriefItemDeliverable();
        deliverable.setBriefItem(briefItem);
        deliverable.setPlatform("youtube");
        deliverable.setAspectRatio("16:9");
        deliverable.setAudioRequired(true);
        deliverable.setCaptionsRequired(false);
        deliverable.setLocalizationRequired(true);
        deliverable.setLocales(new String[]{"en-US", "fr-FR", "de-DE"});
        deliverable.setExtras(Map.of());
        deliverable.setOrderIndex(1);
        deliverable.setIsLocked(false);

        BriefItemDeliverable saved = briefItemDeliverableRepository.save(deliverable);
        entityManager.flush();
        entityManager.clear();

        Optional<BriefItemDeliverable> found = briefItemDeliverableRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLocales()).containsExactly("en-US", "fr-FR", "de-DE");
    }
}
