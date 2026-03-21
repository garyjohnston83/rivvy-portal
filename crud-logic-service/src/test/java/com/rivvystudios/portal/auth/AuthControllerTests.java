package com.rivvystudios.portal.auth;

import com.rivvystudios.portal.TestcontainersConfiguration;
import com.rivvystudios.portal.model.UserAccount;
import com.rivvystudios.portal.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void successfulLoginReturnsUserInfoAndRedirectUrl() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@rivvy.local"))
                .andExpect(jsonPath("$.firstName").value("Rivvy"))
                .andExpect(jsonPath("$.lastName").value("Admin"))
                .andExpect(jsonPath("$.roles[0]").value("RIVVY_ADMIN"))
                .andExpect(jsonPath("$.redirectUrl").value("/admin"));
    }

    @Test
    void loginWithWrongPasswordReturns401WithGenericError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@rivvy.local\",\"password\":\"wrongpassword\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void loginWithNonExistentEmailReturns401WithSameGenericError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@test.com\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void successfulLoginUpdatesLastLoginAt() throws Exception {
        UserAccount before = userAccountRepository.findByEmail("producer@rivvy.local").orElseThrow();
        var beforeLoginAt = before.getLastLoginAt();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"producer@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk());

        UserAccount after = userAccountRepository.findByEmail("producer@rivvy.local").orElseThrow();
        assertThat(after.getLastLoginAt()).isNotNull();
        if (beforeLoginAt != null) {
            assertThat(after.getLastLoginAt()).isAfterOrEqualTo(beforeLoginAt);
        }
    }

    @Test
    void rememberMeTrueSetsLongSessionTimeout() throws Exception {
        // Clear any existing sessions
        jdbcTemplate.update("DELETE FROM SPRING_SESSION");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@rivvy.local\",\"password\":\"password123\",\"rememberMe\":true}"))
                .andExpect(status().isOk());

        // Verify the session's max_inactive_interval in SPRING_SESSION table
        // 180 days = 15552000 seconds
        Integer maxInterval = jdbcTemplate.queryForObject(
                "SELECT MAX_INACTIVE_INTERVAL FROM SPRING_SESSION ORDER BY CREATION_TIME DESC LIMIT 1",
                Integer.class);
        assertThat(maxInterval).isEqualTo(180 * 24 * 60 * 60);
    }

    @Test
    void rememberMeFalseKeepsDefaultSessionTimeout() throws Exception {
        // Clear any existing sessions
        jdbcTemplate.update("DELETE FROM SPRING_SESSION");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk());

        // Verify the session's max_inactive_interval in SPRING_SESSION table
        // 30 minutes = 1800 seconds
        Integer maxInterval = jdbcTemplate.queryForObject(
                "SELECT MAX_INACTIVE_INTERVAL FROM SPRING_SESSION ORDER BY CREATION_TIME DESC LIMIT 1",
                Integer.class);
        assertThat(maxInterval).isEqualTo(1800);
    }

    @Test
    void getAuthMeReturnsUserInfoWhenSessionExists() throws Exception {
        // First, login to create a session
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the session cookie from the response
        jakarta.servlet.http.Cookie[] cookies = loginResult.getResponse().getCookies();
        assertThat(cookies).isNotNull();

        // Use the cookies to call /api/auth/me
        var meRequest = get("/api/auth/me");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            meRequest.cookie(cookie);
        }

        mockMvc.perform(meRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@rivvy.local"))
                .andExpect(jsonPath("$.firstName").value("Rivvy"))
                .andExpect(jsonPath("$.roles[0]").value("RIVVY_ADMIN"));
    }

    @Test
    void adminUserRedirectsToAdmin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@rivvy.local\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redirectUrl").value("/admin"));
    }
}
