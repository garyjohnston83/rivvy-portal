package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.VideoVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoVersionRepository extends JpaRepository<VideoVersion, UUID> {

    Optional<VideoVersion> findByVideoIdAndIsCurrentTrue(UUID videoId);

    Optional<VideoVersion> findFirstByVideoIdOrderByVersionNumberDesc(UUID videoId);

    boolean existsByVideoIdAndIsApprovedTrue(UUID videoId);

    List<VideoVersion> findByVideoIdIn(List<UUID> videoIds);
}
