package com.rivvystudios.portal.brief;

import com.rivvystudios.portal.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BriefControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- Test 1: POST /api/briefs as client creates 201 with defaults ---
    @Test
    void postBriefs_asClient_creates201WithDefaults() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = post("/api/briefs");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.priority").value("NORMAL"))
                .andExpect(jsonPath("$.title").value("Untitled Brief"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orgId").exists())
                .andExpect(jsonPath("$.submittedById").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // --- Test 2: GET /api/briefs/{id} as client returns 200 ---
    @Test
    void getBrief_asClient_returns200() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        // Create a brief first
        String briefId = createBriefAndGetId(cookies);

        var request = get("/api/briefs/" + briefId);
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(briefId))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.title").value("Untitled Brief"));
    }

    // --- Test 3: PUT /api/briefs/{id} as client updates 200 ---
    @Test
    void putBrief_asClient_updates200() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        String briefId = createBriefAndGetId(cookies);

        var request = put("/api/briefs/" + briefId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"My Project\"}");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Project"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    // --- Test 4: DELETE /api/briefs/{id} as client returns 204 ---
    @Test
    void deleteBrief_asClient_returns204() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        String briefId = createBriefAndGetId(cookies);

        var deleteRequest = delete("/api/briefs/" + briefId);
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            deleteRequest.cookie(cookie);
        }

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());

        // Subsequent GET should return 404
        var getRequest = get("/api/briefs/" + briefId);
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            getRequest.cookie(cookie);
        }

        mockMvc.perform(getRequest)
                .andExpect(status().isNotFound());
    }

    // --- Test 5: PUT /api/briefs/{id} as producer returns 403 ---
    @Test
    void putBrief_asProducer_returns403() throws Exception {
        // Create brief as client
        jakarta.servlet.http.Cookie[] clientCookies = loginAs("client2@acme.local");
        String briefId = createBriefAndGetId(clientCookies);

        // Try to update as producer
        jakarta.servlet.http.Cookie[] producerCookies = loginAs("producer@rivvy.local");

        var request = put("/api/briefs/" + briefId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hacked Title\"}");
        for (jakarta.servlet.http.Cookie cookie : producerCookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    // --- Test 6: GET /api/briefs/{id} as producer assigned to org returns 200 ---
    @Test
    void getBrief_asProducer_assignedToOrg_returns200() throws Exception {
        // Create brief as client in Acme Corp
        jakarta.servlet.http.Cookie[] clientCookies = loginAs("client2@acme.local");
        String briefId = createBriefAndGetId(clientCookies);

        // Producer is assigned to Acme Corp via seed data
        jakarta.servlet.http.Cookie[] producerCookies = loginAs("producer@rivvy.local");

        var request = get("/api/briefs/" + briefId);
        for (jakarta.servlet.http.Cookie cookie : producerCookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(briefId));
    }

    // --- Test 7: DELETE /api/briefs/{id} non-draft status returns 409 ---
    @Test
    void deleteBrief_nonDraftStatus_returns409() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        String briefId = createBriefAndGetId(cookies);

        // Manually update status to SUBMITTED via JdbcTemplate
        jdbcTemplate.update("UPDATE brief SET status = 'SUBMITTED' WHERE id = ?::uuid", briefId);

        var deleteRequest = delete("/api/briefs/" + briefId);
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            deleteRequest.cookie(cookie);
        }

        mockMvc.perform(deleteRequest)
                .andExpect(status().isConflict());
    }

    // --- Test 8: POST /api/briefs unauthenticated returns 401 ---
    @Test
    void postBriefs_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/briefs"))
                .andExpect(status().isUnauthorized());
    }

    // --- Helper methods ---

    private jakarta.servlet.http.Cookie[] loginAs(String email) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"password123\",\"rememberMe\":false}"))
                .andExpect(status().isOk())
                .andReturn();

        return loginResult.getResponse().getCookies();
    }

    private String createBriefAndGetId(jakarta.servlet.http.Cookie[] cookies) throws Exception {
        var request = post("/api/briefs");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        // Extract id from JSON response using simple parsing
        int idStart = responseBody.indexOf("\"id\":\"") + 6;
        int idEnd = responseBody.indexOf("\"", idStart);
        return responseBody.substring(idStart, idEnd);
    }
}
