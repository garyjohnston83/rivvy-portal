package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    Page<Video> findByProjectId(UUID projectId, Pageable pageable);
}
