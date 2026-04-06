package com.company.portal.asset.mapper;

import com.company.portal.asset.domain.Asset;
import com.company.portal.asset.domain.AssetHistory;
import com.company.portal.asset.dto.AssetHistoryResponse;
import com.company.portal.asset.dto.AssetRequest;
import com.company.portal.asset.dto.AssetResponse;

public final class AssetMapper {

    private AssetMapper() {}

    public static AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getAssetTag(),
                asset.getSerialNumber(),
                asset.getAssetType(),
                asset.getVendor(),
                asset.getModel(),
                asset.getStatus().name(),
                asset.getPurchaseDate(),
                asset.getWarrantyExpiry(),
                asset.getAssignedUser() != null ? asset.getAssignedUser().getId() : null,
                asset.getAssignedUser() != null ? asset.getAssignedUser().getName() : null,
                asset.getLocation(),
                asset.getCostCenter(),
                asset.getNotes(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    public static Asset toEntity(AssetRequest request) {
        Asset asset = new Asset();
        asset.setAssetTag(request.assetTag());
        asset.setSerialNumber(request.serialNumber());
        asset.setAssetType(request.assetType());
        asset.setVendor(request.vendor());
        asset.setModel(request.model());
        asset.setPurchaseDate(request.purchaseDate());
        asset.setWarrantyExpiry(request.warrantyExpiry());
        asset.setLocation(request.location());
        asset.setCostCenter(request.costCenter());
        asset.setNotes(request.notes());
        return asset;
    }

    public static void updateEntity(Asset asset, AssetRequest request) {
        asset.setAssetTag(request.assetTag());
        asset.setSerialNumber(request.serialNumber());
        asset.setAssetType(request.assetType());
        asset.setVendor(request.vendor());
        asset.setModel(request.model());
        asset.setPurchaseDate(request.purchaseDate());
        asset.setWarrantyExpiry(request.warrantyExpiry());
        asset.setLocation(request.location());
        asset.setCostCenter(request.costCenter());
        asset.setNotes(request.notes());
    }

    public static AssetHistoryResponse toHistoryResponse(AssetHistory history) {
        return new AssetHistoryResponse(
                history.getId(),
                history.getEventType(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getActor() != null ? history.getActor().getId() : null,
                history.getActor() != null ? history.getActor().getName() : null,
                history.getNotes(),
                history.getCreatedAt()
        );
    }
}
