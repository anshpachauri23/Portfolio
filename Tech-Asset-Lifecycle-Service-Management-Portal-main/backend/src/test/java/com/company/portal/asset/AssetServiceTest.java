package com.company.portal.asset;

import com.company.portal.asset.domain.Asset;
import com.company.portal.asset.domain.AssetHistory;
import com.company.portal.asset.domain.AssetStatus;
import com.company.portal.asset.dto.AssetAssignRequest;
import com.company.portal.asset.dto.AssetStatusUpdateRequest;
import com.company.portal.asset.repository.AssetHistoryRepository;
import com.company.portal.asset.repository.AssetRepository;
import com.company.portal.asset.service.AssetService;
import com.company.portal.audit.service.AuditService;
import com.company.portal.notification.service.NotificationService;
import com.company.portal.common.exception.InvalidStateTransitionException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock private AssetRepository assetRepository;
    @Mock private AssetHistoryRepository historyRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetRepository, historyRepository, userRepository, auditService, notificationService);
    }

    private Asset assetWithStatus(AssetStatus status) {
        Asset a = new Asset();
        a.setId(1L);
        a.setAssetTag("TAG-001");
        a.setAssetType("LAPTOP");
        a.setStatus(status);
        return a;
    }

    private User testActor() {
        User u = new User();
        u.setId(99L);
        u.setEmail("actor@company.com");
        return u;
    }

    // --- valid transitions ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_availableToAssigned_succeeds() {
        Asset asset = assetWithStatus(AssetStatus.AVAILABLE);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(assetRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.findByEmail(any())).willReturn(Optional.of(testActor()));
        given(historyRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var request = new AssetStatusUpdateRequest("ASSIGNED", null);
        var result = assetService.updateStatus(1L, request, "actor@company.com");

        assertThat(result.status()).isEqualTo("ASSIGNED");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_assignedToUnderRepair_succeeds() {
        Asset asset = assetWithStatus(AssetStatus.ASSIGNED);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(assetRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.findByEmail(any())).willReturn(Optional.of(testActor()));
        given(historyRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var result = assetService.updateStatus(1L, new AssetStatusUpdateRequest("UNDER_REPAIR", null), "actor@company.com");
        assertThat(result.status()).isEqualTo("UNDER_REPAIR");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_retiredToAssigned_throwsInvalidTransition() {
        Asset asset = assetWithStatus(AssetStatus.RETIRED);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));

        assertThatThrownBy(() ->
                assetService.updateStatus(1L, new AssetStatusUpdateRequest("ASSIGNED", null), "actor@company.com")
        ).isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("RETIRED")
                .hasMessageContaining("ASSIGNED");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_availableToRetired_succeeds() {
        Asset asset = assetWithStatus(AssetStatus.AVAILABLE);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(assetRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.findByEmail(any())).willReturn(Optional.of(testActor()));
        given(historyRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var result = assetService.updateStatus(1L, new AssetStatusUpdateRequest("RETIRED", null), "actor@company.com");
        assertThat(result.status()).isEqualTo("RETIRED");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_underRepairToAssigned_throwsInvalidTransition() {
        Asset asset = assetWithStatus(AssetStatus.UNDER_REPAIR);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));

        assertThatThrownBy(() ->
                assetService.updateStatus(1L, new AssetStatusUpdateRequest("ASSIGNED", null), "actor@company.com")
        ).isInstanceOf(InvalidStateTransitionException.class);
    }

    // --- assign ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignAsset_fromAvailable_writesHistoryAndSetsUser() {
        Asset asset = assetWithStatus(AssetStatus.AVAILABLE);
        User targetUser = new User();
        targetUser.setId(5L);
        targetUser.setName("Emma Employee");
        targetUser.setEmail("emma@company.com");

        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(userRepository.findById(5L)).willReturn(Optional.of(targetUser));
        given(assetRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.findByEmail(any())).willReturn(Optional.of(testActor()));
        given(historyRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var request = new AssetAssignRequest(5L, null);
        var result = assetService.assignAsset(1L, request, "actor@company.com");

        assertThat(result.status()).isEqualTo("ASSIGNED");
        assertThat(result.assignedUserId()).isEqualTo(5L);

        ArgumentCaptor<AssetHistory> historyCaptor = ArgumentCaptor.forClass(AssetHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo("ASSIGNED");
        assertThat(historyCaptor.getValue().getFromStatus()).isEqualTo("AVAILABLE");
        assertThat(historyCaptor.getValue().getToStatus()).isEqualTo("ASSIGNED");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignAsset_targetUserNotFound_throwsNotFound() {
        Asset asset = assetWithStatus(AssetStatus.AVAILABLE);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                assetService.assignAsset(1L, new AssetAssignRequest(999L, null), "actor@company.com")
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    // --- history written on every status change ---

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void updateStatus_alwaysWritesHistoryRecord() {
        Asset asset = assetWithStatus(AssetStatus.AVAILABLE);
        given(assetRepository.findById(1L)).willReturn(Optional.of(asset));
        given(assetRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(userRepository.findByEmail(any())).willReturn(Optional.of(testActor()));
        given(historyRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        assetService.updateStatus(1L, new AssetStatusUpdateRequest("UNDER_REPAIR", "Keyboard broken"), "actor@company.com");

        ArgumentCaptor<AssetHistory> captor = ArgumentCaptor.forClass(AssetHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isEqualTo("AVAILABLE");
        assertThat(captor.getValue().getToStatus()).isEqualTo("UNDER_REPAIR");
        assertThat(captor.getValue().getNotes()).isEqualTo("Keyboard broken");
    }
}
