package com.company.portal.approval;

import com.company.portal.approval.domain.ApprovalDecision;
import com.company.portal.approval.domain.ApprovalStep;
import com.company.portal.approval.dto.ApprovalActionRequest;
import com.company.portal.approval.repository.ApprovalStepRepository;
import com.company.portal.approval.service.ApprovalService;
import com.company.portal.audit.service.AuditService;
import com.company.portal.auth.domain.Role;
import com.company.portal.common.exception.ForbiddenOperationException;
import com.company.portal.common.exception.InvalidStateTransitionException;
import com.company.portal.notification.service.NotificationService;
import com.company.portal.request.domain.RequestStatus;
import com.company.portal.request.domain.RequestType;
import com.company.portal.request.domain.ServiceRequest;
import com.company.portal.request.repository.ServiceRequestRepository;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock private ApprovalStepRepository approvalStepRepository;
    @Mock private ServiceRequestRepository requestRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;
    @Mock private NotificationService notificationService;

    private ApprovalService approvalService;

    @BeforeEach
    void setUp() {
        approvalService = new ApprovalService(
                approvalStepRepository, requestRepository, userRepository, auditService, notificationService);
    }

    private User manager() {
        User u = new User();
        u.setId(10L);
        u.setName("Mike Manager");
        u.setEmail("mike@company.com");
        u.setRole(Role.MANAGER);
        u.setActive(true);
        return u;
    }

    private User employee() {
        User u = new User();
        u.setId(20L);
        u.setName("Emma Employee");
        u.setEmail("emma@company.com");
        u.setRole(Role.EMPLOYEE);
        u.setManager(manager());
        u.setActive(true);
        return u;
    }

    private ServiceRequest pendingRequest() {
        RequestType rt = new RequestType();
        rt.setId(1L);
        rt.setName("New Device");
        rt.setApprovalRequired(true);

        ServiceRequest sr = new ServiceRequest();
        sr.setId(100L);
        sr.setRequestNumber("SR-20260403-00001");
        sr.setTitle("Need new laptop");
        sr.setStatus(RequestStatus.PENDING_APPROVAL);
        sr.setRequester(employee());
        sr.setRequestType(rt);
        sr.setApprovalRequired(true);
        return sr;
    }

    private ApprovalStep pendingStep(ServiceRequest sr, User approver) {
        ApprovalStep step = new ApprovalStep();
        step.setId(1L);
        step.setRequest(sr);
        step.setApprover(approver);
        step.setDecision(ApprovalDecision.PENDING);
        step.setSequenceOrder(1);
        return step;
    }

    @Test
    void createApprovalStep_assignsManagerAsApprover() {
        ServiceRequest sr = pendingRequest();
        given(approvalStepRepository.save(any())).willAnswer(inv -> {
            ApprovalStep s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        ApprovalStep result = approvalService.createApprovalStep(sr);

        assertThat(result.getApprover().getId()).isEqualTo(10L);
        assertThat(result.getDecision()).isEqualTo(ApprovalDecision.PENDING);
        verify(notificationService).notify(any(Long.class), any(), any(), any(), any());
    }

    @Test
    void createApprovalStep_noManager_fallsBackToAnyManager() {
        User noManagerEmployee = employee();
        noManagerEmployee.setManager(null);

        ServiceRequest sr = pendingRequest();
        sr.setRequester(noManagerEmployee);

        User fallbackManager = manager();
        given(userRepository.findFirstByRoleAndActiveTrue(Role.MANAGER)).willReturn(Optional.of(fallbackManager));
        given(approvalStepRepository.save(any())).willAnswer(inv -> {
            ApprovalStep s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        ApprovalStep result = approvalService.createApprovalStep(sr);
        assertThat(result.getApprover().getId()).isEqualTo(10L);
    }

    @Test
    void approve_advancesRequestToInProgress() {
        ServiceRequest sr = pendingRequest();
        User approver = manager();
        ApprovalStep step = pendingStep(sr, approver);

        given(requestRepository.findById(100L)).willReturn(Optional.of(sr));
        given(userRepository.findByEmail("mike@company.com")).willReturn(Optional.of(approver));
        given(approvalStepRepository.findByRequestIdOrderBySequenceOrderAsc(100L)).willReturn(List.of(step));
        given(approvalStepRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(requestRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var result = approvalService.approve(100L, new ApprovalActionRequest("Looks good"), "mike@company.com");

        assertThat(result.decision()).isEqualTo("APPROVED");
        assertThat(sr.getStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
    }

    @Test
    void reject_setsRequestToRejected() {
        ServiceRequest sr = pendingRequest();
        User approver = manager();
        ApprovalStep step = pendingStep(sr, approver);

        given(requestRepository.findById(100L)).willReturn(Optional.of(sr));
        given(userRepository.findByEmail("mike@company.com")).willReturn(Optional.of(approver));
        given(approvalStepRepository.findByRequestIdOrderBySequenceOrderAsc(100L)).willReturn(List.of(step));
        given(approvalStepRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(requestRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var result = approvalService.reject(100L, new ApprovalActionRequest("Budget exceeded"), "mike@company.com");

        assertThat(result.decision()).isEqualTo("REJECTED");
        assertThat(sr.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(sr.getClosedAt()).isNotNull();
    }

    @Test
    void approve_nonPendingRequest_throwsInvalidTransition() {
        ServiceRequest sr = pendingRequest();
        sr.setStatus(RequestStatus.IN_PROGRESS);
        User approver = manager();

        given(requestRepository.findById(100L)).willReturn(Optional.of(sr));
        given(userRepository.findByEmail("mike@company.com")).willReturn(Optional.of(approver));

        assertThatThrownBy(() ->
                approvalService.approve(100L, new ApprovalActionRequest(null), "mike@company.com")
        ).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void approve_wrongApprover_throwsForbidden() {
        ServiceRequest sr = pendingRequest();
        User wrongApprover = new User();
        wrongApprover.setId(999L);
        wrongApprover.setEmail("other@company.com");
        wrongApprover.setRole(Role.MANAGER);

        ApprovalStep step = pendingStep(sr, manager());

        given(requestRepository.findById(100L)).willReturn(Optional.of(sr));
        given(userRepository.findByEmail("other@company.com")).willReturn(Optional.of(wrongApprover));
        given(approvalStepRepository.findByRequestIdOrderBySequenceOrderAsc(100L)).willReturn(List.of(step));

        assertThatThrownBy(() ->
                approvalService.approve(100L, new ApprovalActionRequest(null), "other@company.com")
        ).isInstanceOf(ForbiddenOperationException.class);
    }
}
