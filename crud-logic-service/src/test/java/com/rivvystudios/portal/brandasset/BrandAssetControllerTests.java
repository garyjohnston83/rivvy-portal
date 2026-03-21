package com.rivvystudios.portal.brandasset;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class BrandAssetControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- Test 1: GET /api/brand-assets/counts as authenticated org member returns 200 with all four category keys ---
    @Test
    void getCounts_asOrgMember_returns200WithAllFourCategories() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/brand-assets/counts");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgCounts.logos").exists())
                .andExpect(jsonPath("$.orgCounts.fonts").exists())
                .andExpect(jsonPath("$.orgCounts.guidelines").exists())
                .andExpect(jsonPath("$.orgCounts.visuals").exists());
    }

    // --- Test 2: GET /api/brand-assets/counts as assigned producer returns 200 with valid counts ---
    @Test
    void getCounts_asAssignedProducer_returns200() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("producer@rivvy.local");

        var request = get("/api/brand-assets/counts");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgCounts.logos").exists())
                .andExpect(jsonPath("$.orgCounts.fonts").exists())
                .andExpect(jsonPath("$.orgCounts.guidelines").exists())
                .andExpect(jsonPath("$.orgCounts.visuals").exists());
    }

    // --- Test 3: GET /api/brand-assets/counts unauthenticated returns 401 ---
    @Test
    void getCounts_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/brand-assets/counts"))
                .andExpect(status().isUnauthorized());
    }

    // --- Test 4: GET /api/brand-assets/counts counts only ACTIVE status rows ---
    @Test
    void getCounts_onlyCountsActiveRows() throws Exception {
        // Insert test brand assets with mixed statuses into Acme Corp (org 20..002)
        // created_by client2@acme.local (user 30..004)
        jdbcTemplate.execute(
            "INSERT INTO brand_asset (id, org_id, name, asset_type, tags, visibility, status, created_by, created_at) VALUES " +
            "('a0000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'Active Logo', 'LOGOS', '{}', 'INTERNAL', 'ACTIVE', '30000000-0000-0000-0000-000000000004', NOW())"
        );
        jdbcTemplate.execute(
            "INSERT INTO brand_asset (id, org_id, name, asset_type, tags, visibility, status, created_by, created_at) VALUES " +
            "('a0000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Draft Logo', 'LOGOS', '{}', 'INTERNAL', 'DRAFT', '30000000-0000-0000-0000-000000000004', NOW())"
        );
        jdbcTemplate.execute(
            "INSERT INTO brand_asset (id, org_id, name, asset_type, tags, visibility, status, created_by, created_at) VALUES " +
            "('a0000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', 'Archived Logo', 'LOGOS', '{}', 'INTERNAL', 'ARCHIVED', '30000000-0000-0000-0000-000000000004', NOW())"
        );
        jdbcTemplate.execute(
            "INSERT INTO brand_asset (id, org_id, name, asset_type, tags, visibility, status, created_by, created_at) VALUES " +
            "('a0000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', 'Active Font', 'FONTS', '{}', 'INTERNAL', 'ACTIVE', '30000000-0000-0000-0000-000000000004', NOW())"
        );

        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/brand-assets/counts");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgCounts.logos").value(1))
                .andExpect(jsonPath("$.orgCounts.fonts").value(1))
                .andExpect(jsonPath("$.orgCounts.guidelines").value(0))
                .andExpect(jsonPath("$.orgCounts.visuals").value(0));
    }

    // --- Test 5 (Gap): Response includes projectCounts: null when no projectId param is provided ---
    @Test
    void getCounts_withoutProjectId_returnsNullProjectCounts() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/brand-assets/counts");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgCounts").exists())
                .andExpect(jsonPath("$.projectCounts").doesNotExist());
    }

    // --- Test 6 (Gap): orgCounts defaults all categories to 0 when no brand assets exist for the org ---
    @Test
    void getCounts_noAssetsForOrg_returnsAllZeros() throws Exception {
        // Rivvy Studios org (20..001) has no brand_asset rows in seed data
        // client@rivvy.local is a member of Rivvy Studios
        jakarta.servlet.http.Cookie[] cookies = loginAs("client@rivvy.local");

        var request = get("/api/brand-assets/counts");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgCounts.logos").value(0))
                .andExpect(jsonPath("$.orgCounts.fonts").value(0))
                .andExpect(jsonPath("$.orgCounts.guidelines").value(0))
                .andExpect(jsonPath("$.orgCounts.visuals").value(0));
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
}
