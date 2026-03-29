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
class LockoutIntegrationTests {

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
        Organization org = new Organization();
        org.setName("Integration Test Org");
        org.setSlug("integration-test-org-" + System.nanoTime());
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setCreatedAt(Instant.now());
        org = organizationRepository.save(org);

        testUser = new UserAccount();
        testUser.setEmail("integration-test-" + System.nanoTime() + "@example.com");
        testUser.setFirstName("Integration");
        testUser.setLastName("Test");
        testUser.setPasswordHash(passwordEncoder.encode("correct123"));
        testUser.setStatus(UserAccountStatus.ACTIVE);
        testUser.setDefaultOrg(org);
        testUser.setCreatedAt(Instant.now());
        testUser.setFailedAttemptsCount(0);
        testUser = userAccountRepository.save(testUser);
    }

    @Test
    void fullLockoutWorkflow_failuresLeadToLockout_thenExpiry_thenSuccess() throws Exception {
        // Step 1: Make 5 failed attempts to trigger lockout
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                    .andExpect(status().isUnauthorized());
        }

        // Verify lockout
        UserAccount afterFailures = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(afterFailures.getLockedUntil()).isNotNull();
        assertThat(afterFailures.getFailedAttemptsCount()).isEqualTo(5);

        // Step 2: Attempt login while locked (should be rejected)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));

        // Step 3: Manually expire lockout by setting lockedUntil to past
        afterFailures.setLockedUntil(Instant.now().minusSeconds(1));
        userAccountRepository.save(afterFailures);

        // Step 4: Successful login should clear all lockout fields
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isOk());

        // Verify all lockout fields cleared
        UserAccount afterSuccess = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(afterSuccess.getFailedAttemptsCount()).isEqualTo(0);
        assertThat(afterSuccess.getFirstFailedAttemptAt()).isNull();
        assertThat(afterSuccess.getLastFailedAttemptAt()).isNull();
        assertThat(afterSuccess.getLockedUntil()).isNull();
    }

    @Test
    void unknownEmailWorkflow_noDatabaseMutationsOccur() throws Exception {
        String unknownEmail = "unknown-" + System.nanoTime() + "@example.com";

        // Attempt login with unknown email
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + unknownEmail + "\",\"password\":\"somepassword\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));

        // Verify no user was created
        assertThat(userAccountRepository.findByEmail(unknownEmail)).isEmpty();
    }

    @Test
    void counterResetWorkflow_failedAttempts_thenSuccessfulLogin_countersReset() throws Exception {
        // Make 3 failed attempts
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                    .andExpect(status().isUnauthorized());
        }

        UserAccount afterFailures = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(afterFailures.getFailedAttemptsCount()).isEqualTo(3);

        // Successful login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isOk());

        // Verify counters reset
        UserAccount afterSuccess = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(afterSuccess.getFailedAttemptsCount()).isEqualTo(0);
        assertThat(afterSuccess.getFirstFailedAttemptAt()).isNull();
    }

    @Test
    void windowExpiryReset_failedAttemptOutsideWindow_countersReset() throws Exception {
        // Set a failed attempt that's outside the 15-minute window
        testUser.setFailedAttemptsCount(2);
        testUser.setFirstFailedAttemptAt(Instant.now().minusSeconds(16 * 60)); // 16 minutes ago
        testUser.setLastFailedAttemptAt(Instant.now().minusSeconds(16 * 60));
        userAccountRepository.save(testUser);

        // Make a new failed attempt
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized());

        // Verify window reset: counter should be 1, not 3
        UserAccount updated = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getFailedAttemptsCount()).isEqualTo(1);
        assertThat(updated.getFirstFailedAttemptAt()).isAfter(Instant.now().minusSeconds(60));
    }

    @Test
    void statusTransitionDuringLockout_activeToSuspended() throws Exception {
        // Lock the account
        testUser.setFailedAttemptsCount(5);
        testUser.setLockedUntil(Instant.now().plusSeconds(1800));
        userAccountRepository.save(testUser);

        // Change status to SUSPENDED while locked
        testUser.setStatus(UserAccountStatus.SUSPENDED);
        userAccountRepository.save(testUser);

        // Attempt login with correct password
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Your account has been disabled. Please contact support."));
    }

    @Test
    void databaseConsistency_failedAttemptsSavedCorrectly() throws Exception {
        // Make a failed attempt
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized());

        // Force flush and clear persistence context
        UserAccount fresh = userAccountRepository.findById(testUser.getId()).orElseThrow();

        // Verify all fields persisted correctly
        assertThat(fresh.getFailedAttemptsCount()).isEqualTo(1);
        assertThat(fresh.getFirstFailedAttemptAt()).isNotNull();
        assertThat(fresh.getLastFailedAttemptAt()).isNotNull();
        assertThat(fresh.getLockedUntil()).isNull();
    }

    @Test
    void transactionIsolation_lockoutTriggeredAtomically() throws Exception {
        // Make exactly 5 failed attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"wrong123\",\"rememberMe\":false}"))
                    .andExpect(status().isUnauthorized());
        }

        // Verify lockout was set atomically with counter reaching threshold
        UserAccount locked = userAccountRepository.findById(testUser.getId()).orElseThrow();
        assertThat(locked.getFailedAttemptsCount()).isEqualTo(5);
        assertThat(locked.getLockedUntil()).isNotNull();
        assertThat(locked.getLockedUntil()).isAfter(Instant.now());
    }

    @Test
    void suspendedAccountWithCorrectPassword_revealsDisabledMessage() throws Exception {
        testUser.setStatus(UserAccountStatus.SUSPENDED);
        userAccountRepository.save(testUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + testUser.getEmail() + "\",\"password\":\"correct123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Your account has been disabled. Please contact support."));
    }
}
