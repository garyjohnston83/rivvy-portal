package com.rivvystudios.portal.repository;

import com.rivvystudios.portal.model.BrandAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BrandAssetRepository extends JpaRepository<BrandAsset, UUID> {

    @Query("SELECT b.assetType, COUNT(b) FROM BrandAsset b WHERE b.organization.id = :orgId AND b.status = com.rivvystudios.portal.model.enums.BrandAssetStatus.ACTIVE GROUP BY b.assetType")
    List<Object[]> countByOrgGroupedByType(@Param("orgId") UUID orgId);

    @Query("SELECT b.assetType, COUNT(b) FROM BrandAsset b WHERE b.organization.id = :orgId AND b.project.id = :projectId AND b.status = com.rivvystudios.portal.model.enums.BrandAssetStatus.ACTIVE GROUP BY b.assetType")
    List<Object[]> countByOrgAndProjectGroupedByType(@Param("orgId") UUID orgId, @Param("projectId") UUID projectId);
}
