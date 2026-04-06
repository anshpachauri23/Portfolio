package com.company.portal.asset.repository;

import com.company.portal.asset.domain.Asset;
import com.company.portal.asset.domain.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    boolean existsByAssetTag(String assetTag);

    @Query("""
            SELECT a FROM Asset a
            WHERE (:status IS NULL OR a.status = :status)
              AND (:assetType IS NULL OR a.assetType = :assetType)
              AND (:assignedUserId IS NULL OR a.assignedUser.id = :assignedUserId)
            """)
    Page<Asset> findByFilters(
            @Param("status") AssetStatus status,
            @Param("assetType") String assetType,
            @Param("assignedUserId") Long assignedUserId,
            Pageable pageable
    );
}
