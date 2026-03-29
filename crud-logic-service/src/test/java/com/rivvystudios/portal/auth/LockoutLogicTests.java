package com.rivvystudios.portal.auth;

import com.rivvystudios.portal.TestcontainersConfiguration;
import com.rivvystudios.portal.model.Organization;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.OrganizationStatus;
import com.rivvystudios.portal.model.enums.UserAccountStatus;
import com.rivvystudios.portal.repository.OrganizationRepository;
import com.rivvystudios.portal.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class LockoutLogicTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        // Create test organization
        Organization org = new Organization();
        org.setName("Lockout Test Org");
        org.setSlug("lockout-test-org-" + System.nanoTime());
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());
        org = organizationRepository.save(org);

        // Create test user
        testUser = new UserAccount();
        testUser.setEmail("lockout-test-" + System.nanoTime() + "@example.com");
        testUser.setFirstName("Lockout");
        testUser.setLastName("Test");
        testUser.setPasswordHash(passwordEncoder.encode("correct123"));
        testUser.setStatus(UserAccountStatus.ACTIVE);
        testUser.setDefaultOrg(org);
        testUser.setCreatedAt(Instant.now());
        testUser.setFailedAttemptsCount(0);
        testUser = userAccountRepository.save(testUser);
    }

    @Test
    void lockedAccountReturnsGenericError() throws Exception {
        // Lock the account
        testUser.setLockedUntil(Instant.now().plusSeconds(1800));
        userAccountRepository.save(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void expiredLockoutAllowsAuthenticationAndClearsFields() throws Exception {
        // Set lockout that expired 1 second ago
        testUser.setFailedAttemptsCount(5);
        testUser.setFirstFailedAttemptAt(Instant.now().minusSeconds(900));
        testUser.setLastFailedAttemptAt(Instant.now().minusSeconds(1800));
        testUser.setLockedUntil(Instant.now().minusSeconds(1));
        userAccountRepository.save(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isOk());

        // Verify lockout fields were cleared
        UserAccount updated = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getFailedAttemptsCount()).isEqualTo(0);
        assertThat(updated.getFirstFailedAttemptAt()).isNull();
        assertThat(updated.getLastFailedAttemptAt()).isNull();
        assertThat(updated.getLockedUntil()).isNull();
    }

    @Test
    void failedAttemptIncrementsCounter() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized());

        UserAccount updated = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getFailedAttemptsCount()).isEqualTo(1);
        assertThat(updated.getFirstFailedAttemptAt()).isNotNull();
        assertThat(updated.getLastFailedAttemptAt()).isNotNull();
    }

    @Test
    void multipleFailuresWithinWindowTriggersLockout() throws Exception {
        // Make 5 failed attempts (default threshold)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                    .andExpect(status().isUnauthorized());
        }

        UserAccount updated = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getFailedAttemptsCount()).isEqualTo(5);
        assertThat(updated.getLockedUntil()).isNotNull();
        assertThat(updated.getLockedUntil()).isAfter(Instant.now());
    }

    @Test
    void successfulLoginClearsCounters() throws Exception {
        // Set some failed attempts
        testUser.setFailedAttemptsCount(3);
        testUser.setFirstFailedAttemptAt(Instant.now().minusSeconds(300));
        testUser.setLastFailedAttemptAt(Instant.now().minusSeconds(10));
        userAccountRepository.save(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isOk());

        UserAccount updated = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getFailedAttemptsCount()).isEqualTo(0);
        assertThat(updated.getFirstFailedAttemptAt()).isNull();
        assertThat(updated.getLastFailedAttemptAt()).isNull();
        assertThat(updated.getLockedUntil()).isNull();
    }

    @Test
    void disabledAccountWithCorrectPasswordRevealsDisabledMessage() throws Exception {
        testUser.setStatus(UserAccountStatus.INACTIVE);
        userAccountRepository.save(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Your account has been disabled. Please contact support."));
    }

    @Test
    void disabledAccountWithIncorrectPasswordReturnsGenericError() throws Exception {
        testUser.setStatus(UserAccountStatus.INACTIVE);
        userAccountRepository.save(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void pendingAccountAlwaysReturnsGenericError() throws Exception {
        testUser.setStatus(UserAccountStatus.PENDING);
        userAccountRepository.save(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void lockedAccountDoesNotIncrementCounters() throws Exception {
        // Lock the account and set counters
        testUser.setFailedAttemptsCount(5);
        testUser.setLockedUntil(Instant.now().plusSeconds(1800));
        userAccountRepository.save(testUser);

        // Attempt login while locked
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized());

        // Verify counters remain frozen
        UserAccount updated = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getFailedAttemptsCount()).isEqualTo(5); // Not incremented
    }
}
