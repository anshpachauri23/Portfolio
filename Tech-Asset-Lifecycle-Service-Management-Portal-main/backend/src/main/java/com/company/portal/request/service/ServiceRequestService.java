package com.company.portal.request.service;

import com.company.portal.approval.service.ApprovalService;
import com.company.portal.asset.domain.Asset;
import com.company.portal.asset.repository.AssetRepository;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.common.exception.ForbiddenOperationException;
import com.company.portal.common.exception.InvalidStateTransitionException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.request.domain.*;
import com.company.portal.request.dto.*;
import com.company.portal.request.mapper.ServiceRequestMapper;
import com.company.portal.request.repository.RequestCommentRepository;
import com.company.portal.request.repository.RequestTypeRepository;
import com.company.portal.request.repository.ServiceRequestRepository;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import com.company.portal.audit.service.AuditService;
import com.company.portal.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ServiceRequestService {

    private static final Logger log = LoggerFactory.getLogger(ServiceRequestService.class);

    private static final Map<RequestStatus, Set<RequestStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(RequestStatus.class);
        VALID_TRANSITIONS.put(RequestStatus.DRAFT,            EnumSet.of(RequestStatus.SUBMITTED));
        VALID_TRANSITIONS.put(RequestStatus.SUBMITTED,        EnumSet.of(RequestStatus.PENDING_APPROVAL, RequestStatus.IN_PROGRESS));
        VALID_TRANSITIONS.put(RequestStatus.PENDING_APPROVAL, EnumSet.of(RequestStatus.APPROVED, RequestStatus.REJECTED));
        VALID_TRANSITIONS.put(RequestStatus.APPROVED,         EnumSet.of(RequestStatus.IN_PROGRESS));
        VALID_TRANSITIONS.put(RequestStatus.IN_PROGRESS,      EnumSet.of(RequestStatus.WAITING_FOR_USER, RequestStatus.COMPLETED));
        VALID_TRANSITIONS.put(RequestStatus.WAITING_FOR_USER, EnumSet.of(RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED));
        VALID_TRANSITIONS.put(RequestStatus.COMPLETED,        EnumSet.of(RequestStatus.CLOSED));
        VALID_TRANSITIONS.put(RequestStatus.CLOSED,           EnumSet.noneOf(RequestStatus.class));
        VALID_TRANSITIONS.put(RequestStatus.REJECTED,         EnumSet.noneOf(RequestStatus.class));
    }

    private final ServiceRequestRepository requestRepository;
    private final RequestCommentRepository commentRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final ApprovalService approvalService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ServiceRequestService(
            ServiceRequestRepository requestRepository,
            RequestCommentRepository commentRepository,
            RequestTypeRepository requestTypeRepository,
            UserRepository userRepository,
            AssetRepository assetRepository,
            ApprovalService approvalService,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.requestRepository = requestRepository;
        this.commentRepository = commentRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.approvalService = approvalService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ServiceRequestResponse createRequest(ServiceRequestCreateRequest dto, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);
        RequestType requestType = requestTypeRepository.findById(dto.requestTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RequestType", dto.requestTypeId()));

        Asset asset = null;
        if (dto.assetId() != null) {
            asset = assetRepository.findById(dto.assetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asset", dto.assetId()));
        }

        ServiceRequest sr = new ServiceRequest();
        sr.setRequestNumber(generateRequestNumber());
        sr.setRequestType(requestType);
        sr.setTitle(dto.title());
        sr.setDescription(dto.description());
        sr.setRequester(requester);
        sr.setAsset(asset);
        sr.setPriority(dto.priority());
        sr.setDueDate(dto.dueDate());
        sr.setApprovalRequired(requestType.isApprovalRequired());
        sr.setStatus(requestType.isApprovalRequired() ? RequestStatus.PENDING_APPROVAL : RequestStatus.IN_PROGRESS);

        ServiceRequest saved = requestRepository.save(sr);
        auditService.log("SERVICE_REQUEST", saved.getId().toString(), "CREATE", requesterEmail, null, saved.getRequestNumber());
        log.info("Service request created: {} by {}", saved.getRequestNumber(), requesterEmail);

        if (saved.isApprovalRequired()) {
            approvalService.createApprovalStep(saved);
        }

        List<RequestComment> comments = commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(saved.getId());
        return ServiceRequestMapper.toResponse(saved, comments);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PagedResponse<ServiceRequestResponse> getMyRequests(String requesterEmail, Pageable pageable) {
        User requester = findUserByEmail(requesterEmail);
        Page<ServiceRequest> page = requestRepository.findByRequesterId(requester.getId(), pageable);
        return PagedResponse.from(page.map(sr -> {
            List<RequestComment> comments = commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(sr.getId());
            return ServiceRequestMapper.toResponse(sr, comments);
        }));
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public PagedResponse<ServiceRequestResponse> getQueue(List<RequestStatus> statuses, Pageable pageable) {
        List<RequestStatus> filter = (statuses == null || statuses.isEmpty())
                ? List.of(RequestStatus.SUBMITTED, RequestStatus.PENDING_APPROVAL, RequestStatus.APPROVED, RequestStatus.IN_PROGRESS, RequestStatus.WAITING_FOR_USER)
                : statuses;
        Page<ServiceRequest> page = requestRepository.findByStatusIn(filter, pageable);
        return PagedResponse.from(page.map(sr -> {
            List<RequestComment> comments = commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(sr.getId());
            return ServiceRequestMapper.toResponse(sr, comments);
        }));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequest(Long id, String requestorEmail) {
        ServiceRequest sr = findById(id);
        User user = findUserByEmail(requestorEmail);

        // EMPLOYEE can only see their own requests; other roles can see all
        if (user.getRole().name().equals("EMPLOYEE") && !sr.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("You can only view your own requests");
        }

        List<RequestComment> comments = commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(id);
        return ServiceRequestMapper.toResponse(sr, comments);
    }

    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    @Transactional
    public ServiceRequestResponse updateStatus(Long id, RequestStatusUpdateRequest dto) {
        ServiceRequest sr = findById(id);
        RequestStatus newStatus;
        try {
            newStatus = RequestStatus.valueOf(dto.status());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown request status: " + dto.status());
        }

        validateTransition(sr.getStatus(), newStatus);
        RequestStatus oldStatus = sr.getStatus();
        sr.setStatus(newStatus);

        if (newStatus == RequestStatus.CLOSED || newStatus == RequestStatus.REJECTED) {
            sr.setClosedAt(Instant.now());
        }

        requestRepository.save(sr);
        auditService.log("SERVICE_REQUEST", id.toString(), "STATUS_CHANGE", (Long) null,
                Map.of("status", oldStatus.name()), Map.of("status", newStatus.name()));

        if (newStatus == RequestStatus.COMPLETED) {
            notificationService.notify(sr.getRequester().getId(),
                    "Request Completed: " + sr.getRequestNumber(),
                    "Your request \"" + sr.getTitle() + "\" has been completed.",
                    "SERVICE_REQUEST", id.toString());
        }

        log.info("Request {} status updated to {}", sr.getRequestNumber(), newStatus);

        List<RequestComment> comments = commentRepository.findByServiceRequestIdOrderByCreatedAtAsc(id);
        return ServiceRequestMapper.toResponse(sr, comments);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public RequestCommentResponse addComment(Long requestId, RequestCommentRequest dto, String authorEmail) {
        ServiceRequest sr = findById(requestId);
        User author = findUserByEmail(authorEmail);

        // EMPLOYEE can only comment on their own requests
        if (author.getRole().name().equals("EMPLOYEE") && !sr.getRequester().getId().equals(author.getId())) {
            throw new ForbiddenOperationException("You can only comment on your own requests");
        }

        RequestComment comment = new RequestComment();
        comment.setServiceRequest(sr);
        comment.setAuthor(author);
        comment.setBody(dto.body());

        RequestComment saved = commentRepository.save(comment);
        return ServiceRequestMapper.toCommentResponse(saved);
    }

    public List<RequestTypeResponse> getActiveRequestTypes() {
        return requestTypeRepository.findByActiveTrue()
                .stream()
                .map(ServiceRequestMapper::toTypeResponse)
                .toList();
    }

    // --- helpers ---

    private ServiceRequest findById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void validateTransition(RequestStatus from, RequestStatus to) {
        Set<RequestStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(RequestStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStateTransitionException(from.name(), to.name());
        }
    }

    private String generateRequestNumber() {
        Long seq = requestRepository.nextRequestSequence();
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "SR-" + datePart + "-" + String.format("%05d", seq);
    }
}
