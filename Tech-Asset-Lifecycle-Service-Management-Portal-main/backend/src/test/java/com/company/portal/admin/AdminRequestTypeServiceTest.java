package com.company.portal.admin;

import com.company.portal.admin.dto.RequestTypeRequest;
import com.company.portal.admin.dto.RequestTypeResponse;
import com.company.portal.admin.service.AdminRequestTypeService;
import com.company.portal.audit.service.AuditService;
import com.company.portal.common.exception.DuplicateResourceException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.request.domain.RequestType;
import com.company.portal.request.repository.RequestTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminRequestTypeServiceTest {

    @Mock private RequestTypeRepository requestTypeRepository;
    @Mock private AuditService auditService;

    private AdminRequestTypeService service;

    @BeforeEach
    void setUp() {
        service = new AdminRequestTypeService(requestTypeRepository, auditService);
    }

    private RequestType existingType(Long id, String name, boolean approvalRequired, boolean active) {
        RequestType rt = new RequestType();
        rt.setId(id);
        rt.setName(name);
        rt.setApprovalRequired(approvalRequired);
        rt.setActive(active);
        return rt;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_newName_savesAndReturnsResponse() {
        given(requestTypeRepository.findAll()).willReturn(List.of());
        given(requestTypeRepository.save(any())).willAnswer(inv -> {
            RequestType rt = inv.getArgument(0);
            rt.setId(1L);
            return rt;
        });

        RequestTypeResponse result = service.create(
                new RequestTypeRequest("VPN Access", "Request VPN access", true),
                "admin@company.com");

        assertThat(result.name()).isEqualTo("VPN Access");
        assertThat(result.approvalRequired()).isTrue();
        assertThat(result.active()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_duplicateName_throwsDuplicate() {
        given(requestTypeRepository.findAll()).willReturn(
                List.of(existingType(1L, "VPN Access", true, true)));

        assertThatThrownBy(() -> service.create(new RequestTypeRequest("VPN Access", null, false), "admin@company.com"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("VPN Access");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_caseInsensitiveDuplicateCheck() {
        given(requestTypeRepository.findAll()).willReturn(
                List.of(existingType(1L, "New Device", true, true)));

        assertThatThrownBy(() -> service.create(new RequestTypeRequest("new device", null, false), "admin@company.com"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_changesFieldsAndAudits() {
        RequestType rt = existingType(2L, "Old Name", false, true);
        given(requestTypeRepository.findById(2L)).willReturn(Optional.of(rt));
        given(requestTypeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        RequestTypeResponse result = service.update(2L,
                new RequestTypeRequest("New Name", "Updated description", true),
                "admin@company.com");

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.approvalRequired()).isTrue();
        verify(auditService).log(any(), any(), any(), any(String.class), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_notFound_throwsNotFound() {
        given(requestTypeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.update(999L, new RequestTypeRequest("Name", null, false), "admin@company.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleActive_activeToBecomeInactive() {
        RequestType rt = existingType(3L, "Repair", false, true);
        given(requestTypeRepository.findById(3L)).willReturn(Optional.of(rt));
        given(requestTypeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        RequestTypeResponse result = service.toggleActive(3L, "admin@company.com");
        assertThat(result.active()).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleActive_inactiveToBecomeActive() {
        RequestType rt = existingType(4L, "Return Asset", false, false);
        given(requestTypeRepository.findById(4L)).willReturn(Optional.of(rt));
        given(requestTypeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        RequestTypeResponse result = service.toggleActive(4L, "admin@company.com");
        assertThat(result.active()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAll_returnsAllTypes() {
        given(requestTypeRepository.findAll()).willReturn(List.of(
                existingType(1L, "Type A", false, true),
                existingType(2L, "Type B", true, false)
        ));

        List<RequestTypeResponse> result = service.listAll();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Type A");
        assertThat(result.get(1).name()).isEqualTo("Type B");
    }
}
