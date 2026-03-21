package com.rivvystudios.portal.auth;

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
class PasswordHashMigrationTests {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void passwordHashColumnExistsOnUserAccountTable() {
        // The migration adds password_hash column; Hibernate validation ensures it maps.
        // If the column did not exist, schema validation would fail and the context would not load.
        // We verify by persisting and reading back a UserAccount with a non-null passwordHash.
        Organization org = createOrg("hash-col-org", "hash-col-org");
        UserAccount user = createUser("hashcol@test.com", org);
        user.setPasswordHash("$2a$10$testHashValue");
        userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        UserAccount found = userAccountRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getPasswordHash()).isEqualTo("$2a$10$testHashValue");
    }

    @Test
    void userAccountCanBePersistedAndRetrievedWithPasswordHash() {
        Organization org = createOrg("persist-org", "persist-org");
        UserAccount user = createUser("persist@test.com", org);
        user.setPasswordHash("$2a$10$someHashForTesting12345");
        userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAccount> found = userAccountRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getPasswordHash()).isEqualTo("$2a$10$someHashForTesting12345");
        assertThat(found.get().getEmail()).isEqualTo("persist@test.com");
    }

    @Test
    void findByEmailReturnsCaseInsensitiveMatch() {
        Organization org = createOrg("citext-org", "citext-org");
        UserAccount user = createUser("CaseTest@Example.COM", org);
        user.setPasswordHash("$2a$10$hash");
        userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAccount> found = userAccountRepository.findByEmail("casetest@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByEmailReturnsEmptyForNonExistentEmail() {
        Optional<UserAccount> found = userAccountRepository.findByEmail("nonexistent@nowhere.com");
        assertThat(found).isEmpty();
    }

    private Organization createOrg(String name, String slug) {
        Organization org = new Organization();
        org.setName(name);
        org.setSlug(slug);
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());
        return organizationRepository.save(org);
    }

    private UserAccount createUser(String email, Organization org) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAuthProvider("LOCAL");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());
        return user;
    }
}
