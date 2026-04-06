package com.company.portal.asset;

import com.company.portal.asset.domain.AssetStatus;
import com.company.portal.asset.dto.AssetAssignRequest;
import com.company.portal.asset.dto.AssetRequest;
import com.company.portal.asset.dto.AssetStatusUpdateRequest;
import com.company.portal.asset.repository.AssetHistoryRepository;
import com.company.portal.asset.repository.AssetRepository;
import com.company.portal.asset.service.AssetService;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class AssetIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "dGVzdHNlY3JldGtleWZvcmludGVncmF0aW9udGVzdHMxMjM0NTY3ODk=");
        registry.add("jwt.expiry-ms", () -> "86400000");
    }

    @Autowired private AssetService assetService;
    @Autowired private AssetRepository assetRepository;
    @Autowired private AssetHistoryRepository historyRepository;
    @Autowired private UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void fullAssetLifecycle_createsHistoryAtEachStep() {
        // Create
        var createReq = new AssetRequest("INT-001", "SN-XYZ", "LAPTOP", "Dell", "XPS 15", null, null, null, null, null);
        var created = assetService.createAsset(createReq);
        assertThat(created.status()).isEqualTo("AVAILABLE");

        Long assetId = created.id();

        // Assign to seed user (assumes V2 migration ran — alice@company.com has id=1)
        User alice = userRepository.findByEmail("alice@company.com").orElseThrow();
        assetService.assignAsset(assetId, new AssetAssignRequest(alice.getId(), null), "alice@company.com");

        // Send for repair
        assetService.updateStatus(assetId, new AssetStatusUpdateRequest("UNDER_REPAIR", "Screen cracked"), "alice@company.com");

        // Return to available
        assetService.updateStatus(assetId, new AssetStatusUpdateRequest("AVAILABLE", "Screen replaced"), "alice@company.com");

        // Retire
        assetService.updateStatus(assetId, new AssetStatusUpdateRequest("RETIRED", "EoL"), "alice@company.com");

        // Verify history
        var history = historyRepository.findByAssetIdOrderByCreatedAtDesc(assetId);
        assertThat(history).hasSizeGreaterThanOrEqualTo(4);
        assertThat(history.get(0).getToStatus()).isEqualTo("RETIRED");

        // Verify final DB state
        var asset = assetRepository.findById(assetId).orElseThrow();
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.RETIRED);
    }
}
