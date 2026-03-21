package com.rivvystudios.portal.video;

import com.rivvystudios.portal.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class VideoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private static final String ACME_PROJECT_ID = "70000000-0000-0000-0000-000000000001";
    private static final String VIDEO_1_ID = "90000000-0000-0000-0000-000000000001";
    private static final String VIDEO_3_ID = "90000000-0000-0000-0000-000000000003";
    private static final String VIDEO_4_ID = "90000000-0000-0000-0000-000000000004";
    private static final String STORAGE_OBJECT_1_ID = "91000000-0000-0000-0000-000000000001";

    // --- Test 1: GET /api/videos as Acme client returns 200 with seeded videos ---
    @Test
    void getVideos_asAcmeClient_returns200WithContent() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4));
    }

    // --- Test 2: GET /api/videos as assigned producer returns 200 ---
    @Test
    void getVideos_asAssignedProducer_returns200() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("producer@rivvy.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4));
    }

    // --- Test 3: GET /api/videos unauthenticated returns 401 ---
    @Test
    void getVideos_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0"))
                .andExpect(status().isUnauthorized());
    }

    // --- Test 4: GET /api/videos as non-Acme member returns 403 ---
    @Test
    void getVideos_asNonAcmeMember_returns403() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client@rivvy.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    // --- Test 5: GET /api/videos/{videoId} returns 200 with detail fields ---
    @Test
    void getVideoDetail_asAcmeClient_returns200WithFields() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos/" + VIDEO_1_ID);
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Brand Launch Teaser"))
                .andExpect(jsonPath("$.playbackUrl").value(
                        "https://storage.example.com/stub-presigned/" + STORAGE_OBJECT_1_ID + "?token=stub"))
                .andExpect(jsonPath("$.transcodeStatus").value("COMPLETED"));
    }

    // --- Test 6: GET /api/videos/{nonExistentUuid} returns 404 ---
    @Test
    void getVideoDetail_nonExistentVideo_returns404() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos/00000000-0000-0000-0000-000000000999");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    // --- Test 7: Verify isApproved flag in list response ---
    @Test
    void getVideos_verifyIsApprovedFlag() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        // Videos are sorted by createdAt desc: Video1 (2026-03-14), Video2 (2026-03-13), Video3 (2026-03-12), Video4 (2026-03-11)
        // Video 1 ("Brand Launch Teaser") has isApproved=true
        // Videos 2, 3, 4 have isApproved=false
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].approved").value(true))
                .andExpect(jsonPath("$.content[1].approved").value(false))
                .andExpect(jsonPath("$.content[2].approved").value(false))
                .andExpect(jsonPath("$.content[3].approved").value(false));
    }

    // --- Test 8: Verify pagination fields in list response ---
    @Test
    void getVideos_verifyPaginationFields() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    // ========================================================================
    // Gap-filling tests (Task Group 5)
    // ========================================================================

    // --- Gap Test 1: Verify sort order of videos in list response matches createdAt desc, title asc ---
    @Test
    void getVideos_verifySortOrderCreatedAtDescTitleAsc() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        // Expected order by createdAt desc: Brand Launch Teaser (Mar 14), Product Demo (Mar 13),
        // Behind the Scenes (Mar 12), Social Cutdown (Mar 11)
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Brand Launch Teaser"))
                .andExpect(jsonPath("$.content[1].title").value("Product Demo"))
                .andExpect(jsonPath("$.content[2].title").value("Behind the Scenes"))
                .andExpect(jsonPath("$.content[3].title").value("Social Cutdown"));
    }

    // --- Gap Test 2: Verify video with zero versions returns null currentVersionNumber and false approved ---
    @Test
    void getVideos_videoWithZeroVersions_returnsNullVersionNumberAndFalseApproved() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "0");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        // Video 4 "Social Cutdown" is at index 3 (last in sort order, oldest createdAt)
        // It has zero versions, so currentVersionNumber should be null and approved should be false
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[3].title").value("Social Cutdown"))
                .andExpect(jsonPath("$.content[3].currentVersionNumber").doesNotExist())
                .andExpect(jsonPath("$.content[3].approved").value(false));
    }

    // --- Gap Test 3: Verify page 1 returns empty content when all videos fit on page 0 ---
    @Test
    void getVideos_page1_returnsEmptyContentWhenAllFitOnPage0() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos")
                .param("projectId", ACME_PROJECT_ID)
                .param("page", "1");
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    // --- Gap Test 4: Verify detail for PROCESSING video returns null playbackUrl ---
    @Test
    void getVideoDetail_processingVideo_returnsNullPlaybackUrl() throws Exception {
        jakarta.servlet.http.Cookie[] cookies = loginAs("client2@acme.local");

        var request = get("/api/videos/" + VIDEO_3_ID);
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            request.cookie(cookie);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Behind the Scenes"))
                .andExpect(jsonPath("$.transcodeStatus").value("PROCESSING"))
                .andExpect(jsonPath("$.playbackUrl").doesNotExist())
                .andExpect(jsonPath("$.currentVersionNumber").value(1));
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
