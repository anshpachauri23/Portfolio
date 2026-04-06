package com.company.portal.dashboard.dto;

import java.util.Map;

public record DashboardSummary(
        Map<String, Long> requestCountByStatus,
        Map<String, Long> assetCountByStatus
) {}
