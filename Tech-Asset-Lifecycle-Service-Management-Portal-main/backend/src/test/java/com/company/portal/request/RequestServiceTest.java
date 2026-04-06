package com.company.portal.request;

import com.company.portal.approval.service.ApprovalService;
import com.company.portal.asset.repository.AssetRepository;
import com.company.portal.audit.service.AuditService;
import com.company.portal.notification.service.NotificationService;
import com.company.portal.common.exception.InvalidStateTransitionException;
import com.company.portal.request.domain.RequestStatus;
import com.company.portal.request.domain.RequestType;
import com.company.portal.request.domain.ServiceRequest;
import com.company.portal.request.dto.RequestStatusUpdateRequest;
import com.company.portal.request.dto.ServiceRequestCreateRequest;
import com.company.portal.request.repository.RequestCommentRepository;
import com.company.portal.request.repository.RequestTypeRepository;
import com.company.portal.request.repository.ServiceRequestRepository;
import com.company.portal.request.service.ServiceRequestService;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock private ServiceRequestRepository requestRepository;
    @Mock private RequestCommentRepository commentRepository;
    @Mock private RequestTypeRepository requestTypeRepository;
    @Mock private UserRepository userRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private ApprovalService approvalService;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;

    private ServiceRequestService service;

    @BeforeEach
    void setUp() {
        service = new ServiceRequestService(
                requestRepository, commentRepository, requestTypeRepository,
                userRepository, assetRepository, approvalService, auditService, notificationService);
    }

    private User testUser(String email) {
        User u = new User();
        u.setId(1L);
        u.setName("Test User");
        u.setEmail(email);
        return u;
    }

    private RequestType requestType(boolean approvalRequired) {
        RequestType rt = new RequestType();
        rt.setId(1L);
        rt.setName("New Device");
        rt.setApprovalRequired(approvalRequired);
        rt.setActive(true);
        return rt;
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createRequest_approvalRequired_setsPendingApprovalStatus() {
        given(userRepository.findByEmail("emp@company.com")).willReturn(Optional.of(testUser("emp@company.com")));
        given(requestTypeRepository.findById(1L)).willReturn(Optional.of(requestType(true)));
        given(requestRepository.nextRequestSequence()).willReturn(1001L);
        given(requestRepository.save(any())).willAnswer(inv -> {
            ServiceRequest sr = inv.getArgument(0);
            sr.setId(10L);
            return sr;
        });
        given(commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(10L)).willReturn(List.of());

        var dto = new ServiceRequestCreateRequest(1L, "Need a laptop", "I need a new laptop for work", null, "MEDIUM", null);
        var result = service.createRequest(dto, "emp@company.com");

        assertThat(result.status()).isEqualTo(RequestStatus.PENDING_APPROVAL.name());
        assertThat(result.approvalRequired()).isTrue();
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createRequest_noApprovalRequired_setsInProgressStatus() {
        given(userRepository.findByEmail("emp@company.com")).willReturn(Optional.of(testUser("emp@company.com")));
        given(requestTypeRepository.findById(1L)).willReturn(Optional.of(requestType(false)));
        given(requestRepository.nextRequestSequence()).willReturn(1002L);
        given(requestRepository.save(any())).willAnswer(inv -> {
            ServiceRequest sr = inv.getArgument(0);
            sr.setId(11L);
            return sr;
        });
        given(commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(11L)).willReturn(List.of());

        var dto = new ServiceRequestCreateRequest(1L, "Repair keyboard", "The keyboard is not working", null, "HIGH", null);
        var result = service.createRequest(dto, "emp@company.com");

        assertThat(result.status()).isEqualTo(RequestStatus.IN_PROGRESS.name());
        assertThat(result.approvalRequired()).isFalse();
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void updateStatus_validTransition_succeeds() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setStatus(RequestStatus.IN_PROGRESS);
        RequestType rt = requestType(false);
        User requester = testUser("emp@company.com");
        sr.setRequestType(rt);
        sr.setRequester(requester);

        given(requestRepository.findById(1L)).willReturn(Optional.of(sr));
        given(requestRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(1L)).willReturn(List.of());

        var result = service.updateStatus(1L, new RequestStatusUpdateRequest("WAITING_FOR_USER", null));
        assertThat(result.status()).isEqualTo("WAITING_FOR_USER");
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void updateStatus_invalidTransition_throwsException() {
        ServiceRequest sr = new ServiceRequest();
        sr.setId(1L);
        sr.setStatus(RequestStatus.CLOSED);

        given(requestRepository.findById(1L)).willReturn(Optional.of(sr));

        assertThatThrownBy(() ->
                service.updateStatus(1L, new RequestStatusUpdateRequest("IN_PROGRESS", null))
        ).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createRequest_requestNumberFormat_startsWithSR() {
        given(userRepository.findByEmail("emp@company.com")).willReturn(Optional.of(testUser("emp@company.com")));
        given(requestTypeRepository.findById(1L)).willReturn(Optional.of(requestType(false)));
        given(requestRepository.nextRequestSequence()).willReturn(1003L);
        given(requestRepository.save(any())).willAnswer(inv -> {
            ServiceRequest sr = inv.getArgument(0);
            sr.setId(12L);
            return sr;
        });
        given(commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(12L)).willReturn(List.of());

        var dto = new ServiceRequestCreateRequest(1L, "Return old laptop", "Returning the old laptop as requested", null, "LOW", null);
        var result = service.createRequest(dto, "emp@company.com");

        assertThat(result.requestNumber()).startsWith("SR-");
    }
}
