package com.rivvystudios.portal.auth;

import com.rivvystudios.portal.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postLoginEndpointIsAccessibleWithoutAuthentication() throws Exception {
        // POST /api/auth/login with bad credentials should return 401, not 403
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad@test.com\",\"password\":\"wrong\",\"rememberMe\":false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAuthMeReturns401WhenNoSessionPresent() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedApiEndpointReturns401ForUnauthenticatedRequest() throws Exception {
        // A protected API endpoint should return 401, not a redirect
        mockMvc.perform(get("/api/some-protected-endpoint")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
