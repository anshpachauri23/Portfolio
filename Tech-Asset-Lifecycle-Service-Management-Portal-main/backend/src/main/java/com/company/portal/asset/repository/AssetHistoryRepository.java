package com.company.portal.asset.repository;

import com.company.portal.asset.domain.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {

    List<AssetHistory> findByAssetIdOrderByCreatedAtDesc(Long assetId);
}
