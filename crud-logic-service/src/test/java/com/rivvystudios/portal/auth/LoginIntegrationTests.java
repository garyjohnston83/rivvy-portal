package com.rivvystudios.portal.auth;

import com.rivvystudios.portal.TestcontainersConfiguration;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.model.enums.UserAccountStatus;
import com.rivvystudios.portal.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void loginWithProducerUserRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"producer@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl").value("/dashboard"))
                .andExpect(jsonPath("$.roles[0]").value("RIVVY_PRODUCER"));
    }

    @Test
    void loginWithClientUserRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"client@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl").value("/dashboard"))
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"));
    }

    @Test
    void loginWithDisabledAccountReturns401WithGenericError() throws Exception {
        // Temporarily set admin to SUSPENDED status
        UserAccount user = userAccountRepository.findByEmail("admin@rivvy.local").orElseThrow();
        UserAccountStatus originalStatus = user.getStatus();
        user.setStatus(UserAccountStatus.SUSPENDED);
        userAccountRepository.save(user);

        try {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"admin@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid email or password"));
        } finally {
            // Restore original status
            user.setStatus(originalStatus);
            userAccountRepository.save(user);
        }
    }

    @Test
    void userDetailsServiceReturnsDisabledForNonActiveUser() {
        // Temporarily set a user to INACTIVE
        UserAccount user = userAccountRepository.findByEmail("client@rivvy.local").orElseThrow();
        UserAccountStatus originalStatus = user.getStatus();
        user.setStatus(UserAccountStatus.INACTIVE);
        userAccountRepository.save(user);

        try {
            UserDetails details = userDetailsService.loadUserByUsername("client@rivvy.local");
            assertThat(details.isEnabled()).isFalse();
        } finally {
            user.setStatus(originalStatus);
            userAccountRepository.save(user);
        }
    }

    @Test
    void getAuthMeWorksWithSessionCookieFromPriorLogin() throws Exception {
        // Login to get a session
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"producer@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andReturn();

        jakarta.servlet.http.Cookie[] cookies = loginResult.getResponse().getCookies();

        // Use session cookie to call /api/auth/me
        var meRequest = get("/api/auth/me");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            meRequest.cookie(cookie);
        }

        mockMvc.perform(meRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("producer@rivvy.local"))
                .andExpect(jsonPath("$.firstName").value("Rivvy"))
                .andExpect(jsonPath("$.lastName").value("Producer"))
                .andExpect(jsonPath("$.roles[0]").value("RIVVY_PRODUCER"));
    }

    @Test
    void corsHeadersPresentForCrossOriginRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Origin", "http://localhost:5200")
                .content("{\"email\":\"admin@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String allowOrigin = result.getResponse().getHeader("Access-Control-Allow-Origin");
                    assertThat(allowOrigin).isEqualTo("http://localhost:5200");
                });
    }
}
