package com.rivvystudios.portal.persistence;

import com.rivvystudios.portal.TestcontainersConfiguration;
import com.rivvystudios.portal.model.Organization;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.OrganizationStatus;
import com.rivvystudios.portal.model.enums.UserAccountStatus;
import com.rivvystudios.portal.repository.OrganizationRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EntityCrudSmokeTests {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createAndReadOrganization() {
        Organization org = new Organization();
        org.setName("Test Organization");
        org.setSlug("test-organization");
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());

        Organization saved = organizationRepository.save(org);
        entityManager.flush();
        entityManager.clear();

        Optional<Organization> found = organizationRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Organization");
        assertThat(found.get().getSlug()).isEqualTo("test-organization");
        assertThat(found.get().getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
    }

    @Test
    void createAndReadUserAccountWithForeignKey() {
        Organization org = new Organization();
        org.setName("FK Test Org");
        org.setSlug("fk-test-org");
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());
        Organization savedOrg = organizationRepository.save(org);

        UserAccount user = new UserAccount();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAuthProvider("LOCAL");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(savedOrg);
        user.setCreatedAt(Instant.now());

        UserAccount savedUser = userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAccount> found = userAccountRepository.findById(savedUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(found.get().getLastName()).isEqualTo("User");
        assertThat(found.get().getStatus()).isEqualTo(UserAccountStatus.ACTIVE);
        // Verify lazy-loaded FK relationship works
        assertThat(found.get().getDefaultOrg()).isNotNull();
        assertThat(found.get().getDefaultOrg().getName()).isEqualTo("FK Test Org");
    }

    @Test
    void updateOrganization() {
        Organization org = new Organization();
        org.setName("Original Name");
        org.setSlug("original-name");
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());
        Organization saved = organizationRepository.save(org);
        entityManager.flush();
        entityManager.clear();

        Organization toUpdate = organizationRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setName("Updated Name");
        organizationRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        Organization updated = organizationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteOrganization() {
        Organization org = new Organization();
        org.setName("To Delete");
        org.setSlug("to-delete");
        org.setStatus(OrganizationStatus.INACTIVE);
        org.setCreatedAt(Instant.now());
        Organization saved = organizationRepository.save(org);
        entityManager.flush();

        organizationRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        Optional<Organization> found = organizationRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}
