package com.company.portal.asset.dto;

import java.time.Instant;
import java.time.LocalDate;

public record AssetResponse(
        Long id,
        String assetTag,
        String serialNumber,
        String assetType,
        String vendor,
        String model,
        String status,
        LocalDate purchaseDate,
        LocalDate warrantyExpiry,
        Long assignedUserId,
        String assignedUserName,
        String location,
        String costCenter,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
