package com.company.portal.approval.service;

import com.company.portal.approval.domain.ApprovalDecision;
import com.company.portal.approval.domain.ApprovalStep;
import com.company.portal.approval.dto.ApprovalActionRequest;
import com.company.portal.approval.dto.ApprovalStepResponse;
import com.company.portal.approval.mapper.ApprovalMapper;
import com.company.portal.approval.repository.ApprovalStepRepository;
import com.company.portal.audit.service.AuditService;
import com.company.portal.auth.domain.Role;
import com.company.portal.notification.service.NotificationService;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.common.exception.ForbiddenOperationException;
import com.company.portal.common.exception.InvalidStateTransitionException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.request.domain.RequestStatus;
import com.company.portal.request.domain.ServiceRequest;
import com.company.portal.request.repository.ServiceRequestRepository;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    private final ApprovalStepRepository approvalStepRepository;
    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ApprovalService(
            ApprovalStepRepository approvalStepRepository,
            ServiceRequestRepository requestRepository,
            UserRepository userRepository,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.approvalStepRepository = approvalStepRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ApprovalStep createApprovalStep(ServiceRequest request) {
        User approver = resolveApprover(request.getRequester());

        ApprovalStep step = new ApprovalStep();
        step.setRequest(request);
        step.setApprover(approver);
        step.setDecision(ApprovalDecision.PENDING);
        step.setSequenceOrder(1);

        ApprovalStep saved = approvalStepRepository.save(step);
        notificationService.notify(approver.getId(),
                "Approval Required: " + request.getRequestNumber(),
                "Request \"" + request.getTitle() + "\" needs your approval.",
                "SERVICE_REQUEST", request.getId().toString());
        log.info("Approval step created for request {} assigned to {}",
                request.getRequestNumber(), approver.getEmail());
        return saved;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public ApprovalStepResponse approve(Long requestId, ApprovalActionRequest dto, String approverEmail) {
        ServiceRequest request = findRequest(requestId);
        User approver = findUserByEmail(approverEmail);

        validateApprovalAction(request, approver);

        ApprovalStep step = findPendingStep(requestId, approver.getId());
        step.setDecision(ApprovalDecision.APPROVED);
        step.setComment(dto.comment());
        step.setDecidedAt(Instant.now());
        approvalStepRepository.save(step);

        request.setStatus(RequestStatus.IN_PROGRESS);
        requestRepository.save(request);

        auditService.log("SERVICE_REQUEST", requestId.toString(), "APPROVE", approverEmail,
                Map.of("status", "PENDING_APPROVAL"), Map.of("status", "IN_PROGRESS"));
        notificationService.notify(request.getRequester().getId(),
                "Request Approved: " + request.getRequestNumber(),
                "Your request \"" + request.getTitle() + "\" has been approved.",
                "SERVICE_REQUEST", requestId.toString());
        log.info("Request {} approved by {}", request.getRequestNumber(), approverEmail);
        return ApprovalMapper.toResponse(step);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public ApprovalStepResponse reject(Long requestId, ApprovalActionRequest dto, String approverEmail) {
        ServiceRequest request = findRequest(requestId);
        User approver = findUserByEmail(approverEmail);

        validateApprovalAction(request, approver);

        ApprovalStep step = findPendingStep(requestId, approver.getId());
        step.setDecision(ApprovalDecision.REJECTED);
        step.setComment(dto.comment());
        step.setDecidedAt(Instant.now());
        approvalStepRepository.save(step);

        request.setStatus(RequestStatus.REJECTED);
        request.setClosedAt(Instant.now());
        requestRepository.save(request);

        auditService.log("SERVICE_REQUEST", requestId.toString(), "REJECT", approverEmail,
                Map.of("status", "PENDING_APPROVAL"), Map.of("status", "REJECTED"));
        notificationService.notify(request.getRequester().getId(),
                "Request Rejected: " + request.getRequestNumber(),
                "Your request \"" + request.getTitle() + "\" has been rejected.",
                "SERVICE_REQUEST", requestId.toString());
        log.info("Request {} rejected by {}", request.getRequestNumber(), approverEmail);
        return ApprovalMapper.toResponse(step);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional(readOnly = true)
    public PagedResponse<ApprovalStepResponse> getPendingApprovals(String approverEmail, Pageable pageable) {
        User approver = findUserByEmail(approverEmail);
        Page<ApprovalStep> page = approvalStepRepository
                .findByApproverIdAndDecision(approver.getId(), ApprovalDecision.PENDING, pageable);
        return PagedResponse.from(page.map(ApprovalMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ApprovalStepResponse> getApprovalHistory(Long requestId) {
        return approvalStepRepository.findByRequestIdOrderBySequenceOrderAsc(requestId)
                .stream()
                .map(ApprovalMapper::toResponse)
                .toList();
    }

    private User resolveApprover(User requester) {
        if (requester.getManager() != null) {
            return requester.getManager();
        }
        return userRepository.findFirstByRoleAndActiveTrue(Role.MANAGER)
                .orElseThrow(() -> new ResourceNotFoundException("No available MANAGER user found for approval"));
    }

    private void validateApprovalAction(ServiceRequest request, User approver) {
        if (request.getStatus() != RequestStatus.PENDING_APPROVAL) {
            throw new InvalidStateTransitionException(
                    request.getStatus().name(), "APPROVED/REJECTED (request is not PENDING_APPROVAL)");
        }

        boolean isAssigned = approvalStepRepository
                .findByRequestIdOrderBySequenceOrderAsc(request.getId())
                .stream()
                .anyMatch(s -> s.getApprover().getId().equals(approver.getId())
                        && s.getDecision() == ApprovalDecision.PENDING);

        if (!isAssigned) {
            throw new ForbiddenOperationException("You are not assigned as an approver for this request");
        }
    }

    private ApprovalStep findPendingStep(Long requestId, Long approverId) {
        return approvalStepRepository.findByRequestIdOrderBySequenceOrderAsc(requestId)
                .stream()
                .filter(s -> s.getApprover().getId().equals(approverId)
                        && s.getDecision() == ApprovalDecision.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No pending approval step found"));
    }

    private ServiceRequest findRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
