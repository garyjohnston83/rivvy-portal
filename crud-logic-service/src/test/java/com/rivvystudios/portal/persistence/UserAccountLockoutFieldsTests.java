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
class UserAccountLockoutFieldsTests {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EntityManager entityManager;

    private Organization createTestOrganization() {
        Organization org = new Organization();
        org.setName("Lockout Test Org");
        org.setSlug("lockout-test-org");
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());
        return organizationRepository.save(org);
    }

    @Test
    void lockoutFieldsDefaultToZeroAndNull() {
        Organization org = createTestOrganization();

        UserAccount user = new UserAccount();
        user.setEmail("lockout-default-" + System.nanoTime() + "@example.com");
        user.setFirstName("Default");
        user.setLastName("Test");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());

        UserAccount saved = userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAccount> found = userAccountRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFailedAttemptsCount()).isEqualTo(0);
        assertThat(found.get().getFirstFailedAttemptAt()).isNull();
        assertThat(found.get().getLastFailedAttemptAt()).isNull();
        assertThat(found.get().getLockedUntil()).isNull();
    }

    @Test
    void lockoutFieldsPersistCorrectly() {
        Organization org = createTestOrganization();
        Instant now = Instant.now();
        Instant firstAttempt = now.minusSeconds(300);
        Instant lastAttempt = now.minusSeconds(10);
        Instant lockoutExpiry = now.plusSeconds(1800);

        UserAccount user = new UserAccount();
        user.setEmail("lockout-persist-" + System.nanoTime() + "@example.com");
        user.setFirstName("Persist");
        user.setLastName("Test");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());
        user.setFailedAttemptsCount(5);
        user.setFirstFailedAttemptAt(firstAttempt);
        user.setLastFailedAttemptAt(lastAttempt);
        user.setLockedUntil(lockoutExpiry);

        UserAccount saved = userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAccount> found = userAccountRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFailedAttemptsCount()).isEqualTo(5);
        assertThat(found.get().getFirstFailedAttemptAt()).isEqualTo(firstAttempt);
        assertThat(found.get().getLastFailedAttemptAt()).isEqualTo(lastAttempt);
        assertThat(found.get().getLockedUntil()).isEqualTo(lockoutExpiry);
    }

    @Test
    void lockoutFieldsCanBeUpdated() {
        Organization org = createTestOrganization();
        Instant now = Instant.now();

        UserAccount user = new UserAccount();
        user.setEmail("lockout-update-" + System.nanoTime() + "@example.com");
        user.setFirstName("Update");
        user.setLastName("Test");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());
        user.setFailedAttemptsCount(0);

        UserAccount saved = userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Retrieve and update
        UserAccount retrieved = userAccountRepository.findById(saved.getId()).orElseThrow();
        retrieved.setFailedAttemptsCount(3);
        retrieved.setFirstFailedAttemptAt(now.minusSeconds(600));
        retrieved.setLastFailedAttemptAt(now);
        userAccountRepository.save(retrieved);
        entityManager.flush();
        entityManager.clear();

        // Verify update
        Optional<UserAccount> updated = userAccountRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getFailedAttemptsCount()).isEqualTo(3);
        assertThat(updated.get().getFirstFailedAttemptAt()).isNotNull();
        assertThat(updated.get().getLastFailedAttemptAt()).isNotNull();
    }

    @Test
    void lockoutFieldsCanBeCleared() {
        Organization org = createTestOrganization();
        Instant now = Instant.now();

        UserAccount user = new UserAccount();
        user.setEmail("lockout-clear-" + System.nanoTime() + "@example.com");
        user.setFirstName("Clear");
        user.setLastName("Test");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());
        user.setFailedAttemptsCount(5);
        user.setFirstFailedAttemptAt(now.minusSeconds(300));
        user.setLastFailedAttemptAt(now);
        user.setLockedUntil(now.plusSeconds(1800));

        UserAccount saved = userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Clear lockout fields
        UserAccount retrieved = userAccountRepository.findById(saved.getId()).orElseThrow();
        retrieved.setFailedAttemptsCount(0);
        retrieved.setFirstFailedAttemptAt(null);
        retrieved.setLastFailedAttemptAt(null);
        retrieved.setLockedUntil(null);
        userAccountRepository.save(retrieved);
        entityManager.flush();
        entityManager.clear();

        // Verify cleared
        Optional<UserAccount> cleared = userAccountRepository.findById(saved.getId());
        assertThat(cleared).isPresent();
        assertThat(cleared.get().getFailedAttemptsCount()).isEqualTo(0);
        assertThat(cleared.get().getFirstFailedAttemptAt()).isNull();
        assertThat(cleared.get().getLastFailedAttemptAt()).isNull();
        assertThat(cleared.get().getLockedUntil()).isNull();
    }

    @Test
    void timestampFieldsHandleInstantType() {
        Organization org = createTestOrganization();
        Instant preciseInstant = Instant.parse("2026-03-29T10:15:30.123456Z");

        UserAccount user = new UserAccount();
        user.setEmail("lockout-instant-" + System.nanoTime() + "@example.com");
        user.setFirstName("Instant");
        user.setLastName("Test");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setDefaultOrg(org);
        user.setCreatedAt(Instant.now());
        user.setFirstFailedAttemptAt(preciseInstant);
        user.setLastFailedAttemptAt(preciseInstant.plusSeconds(60));
        user.setLockedUntil(preciseInstant.plusSeconds(1800));

        UserAccount saved = userAccountRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<UserAccount> found = userAccountRepository.findById(saved.getId());
        assertThat(found).isPresent();
        // PostgreSQL timestamptz stores microsecond precision
        assertThat(found.get().getFirstFailedAttemptAt()).isEqualTo(preciseInstant);
        assertThat(found.get().getLastFailedAttemptAt()).isEqualTo(preciseInstant.plusSeconds(60));
        assertThat(found.get().getLockedUntil()).isEqualTo(preciseInstant.plusSeconds(1800));
    }
}
