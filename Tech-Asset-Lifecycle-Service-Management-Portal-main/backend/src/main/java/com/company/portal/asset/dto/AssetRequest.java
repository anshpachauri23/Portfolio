package com.company.portal.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AssetRequest(
        @NotBlank(message = "Asset tag is required")
        @Size(max = 50, message = "Asset tag must not exceed 50 characters")
        String assetTag,

        @Size(max = 100)
        String serialNumber,

        @NotBlank(message = "Asset type is required")
        @Size(max = 50)
        String assetType,

        @Size(max = 100)
        String vendor,

        @Size(max = 100)
        String model,

        LocalDate purchaseDate,
        LocalDate warrantyExpiry,

        @Size(max = 200)
        String location,

        @Size(max = 100)
        String costCenter,

        String notes
) {}
