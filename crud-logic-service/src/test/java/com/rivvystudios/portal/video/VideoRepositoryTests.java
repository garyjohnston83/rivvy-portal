package com.rivvystudios.portal.video;

import com.rivvystudios.portal.TestcontainersConfiguration;
import com.rivvystudios.portal.model.Video;
import com.rivvystudios.portal.model.VideoVersion;
import com.rivvystudios.portal.repository.VideoRepository;
import com.rivvystudios.portal.repository.VideoVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class VideoRepositoryTests {

    // Seed data UUIDs from R__seed_data.sql
    private static final UUID ACME_PROJECT_ID = UUID.fromString("70000000-0000-0000-0000-000000000001");
    private static final UUID VIDEO_1_ID = UUID.fromString("90000000-0000-0000-0000-000000000001"); // Brand Launch Teaser, approved
    private static final UUID VIDEO_2_ID = UUID.fromString("90000000-0000-0000-0000-000000000002"); // Product Demo, unapproved
    private static final UUID VIDEO_3_ID = UUID.fromString("90000000-0000-0000-0000-000000000003"); // Behind the Scenes, processing
    private static final UUID VIDEO_4_ID = UUID.fromString("90000000-0000-0000-0000-000000000004"); // Social Cutdown, no versions

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoVersionRepository videoVersionRepository;

    @Test
    void findByProjectId_returnsPaginatedResultsSortedByCreatedAtDesc() {
        Page<Video> page = videoRepository.findByProjectId(
                ACME_PROJECT_ID,
                PageRequest.of(0, 25, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("title")))
        );

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).hasSize(4);
        // Verify descending createdAt order: Video 1 (Mar 14), Video 2 (Mar 13), Video 3 (Mar 12), Video 4 (Mar 11)
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("Brand Launch Teaser");
        assertThat(page.getContent().get(1).getTitle()).isEqualTo("Product Demo");
        assertThat(page.getContent().get(2).getTitle()).isEqualTo("Behind the Scenes");
        assertThat(page.getContent().get(3).getTitle()).isEqualTo("Social Cutdown");
    }

    @Test
    void findByVideoIdAndIsCurrentTrue_returnsCurrentVersion() {
        Optional<VideoVersion> result = videoVersionRepository.findByVideoIdAndIsCurrentTrue(VIDEO_1_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getVersionNumber()).isEqualTo(1);
        assertThat(result.get().getIsCurrent()).isTrue();
        assertThat(result.get().getIsApproved()).isTrue();
    }

    @Test
    void findByVideoIdAndIsCurrentTrue_returnsEmptyWhenNoCurrentVersion() {
        // Video 3 has isCurrent=false
        Optional<VideoVersion> result = videoVersionRepository.findByVideoIdAndIsCurrentTrue(VIDEO_3_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByVideoIdOrderByVersionNumberDesc_returnsFallbackVersion() {
        // Video 3 has no isCurrent=true version, but has a version with versionNumber=1
        Optional<VideoVersion> result = videoVersionRepository.findFirstByVideoIdOrderByVersionNumberDesc(VIDEO_3_ID);

        assertThat(result).isPresent();
        assertThat(result.get().getVersionNumber()).isEqualTo(1);
    }

    @Test
    void existsByVideoIdAndIsApprovedTrue_returnsCorrectBoolean() {
        // Video 1 has an approved version
        assertThat(videoVersionRepository.existsByVideoIdAndIsApprovedTrue(VIDEO_1_ID)).isTrue();

        // Video 2 has no approved version
        assertThat(videoVersionRepository.existsByVideoIdAndIsApprovedTrue(VIDEO_2_ID)).isFalse();

        // Video 4 has no versions at all
        assertThat(videoVersionRepository.existsByVideoIdAndIsApprovedTrue(VIDEO_4_ID)).isFalse();
    }

    @Test
    void findByVideoIdIn_batchFetchesVersionsForMultipleVideoIds() {
        List<VideoVersion> versions = videoVersionRepository.findByVideoIdIn(
                List.of(VIDEO_1_ID, VIDEO_2_ID, VIDEO_3_ID, VIDEO_4_ID)
        );

        // Videos 1, 2, and 3 each have one version; Video 4 has none
        assertThat(versions).hasSize(3);

        List<UUID> videoIds = versions.stream()
                .map(vv -> vv.getVideo().getId())
                .toList();
        assertThat(videoIds).containsExactlyInAnyOrder(VIDEO_1_ID, VIDEO_2_ID, VIDEO_3_ID);
    }
}
