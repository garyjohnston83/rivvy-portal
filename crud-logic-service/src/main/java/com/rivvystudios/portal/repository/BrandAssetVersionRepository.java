package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.BrandAssetVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BrandAssetVersionRepository extends JpaRepository<BrandAssetVersion, UUID> {
}
