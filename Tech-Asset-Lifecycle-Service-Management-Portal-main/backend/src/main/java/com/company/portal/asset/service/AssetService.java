package com.company.portal.asset.service;

import com.company.portal.asset.domain.Asset;
import com.company.portal.asset.domain.AssetHistory;
import com.company.portal.asset.domain.AssetStatus;
import com.company.portal.asset.dto.*;
import com.company.portal.asset.mapper.AssetMapper;
import com.company.portal.asset.repository.AssetHistoryRepository;
import com.company.portal.asset.repository.AssetRepository;
import com.company.portal.audit.service.AuditService;
import com.company.portal.notification.service.NotificationService;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.common.exception.DuplicateResourceException;
import com.company.portal.common.exception.InvalidStateTransitionException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);

    private static final Map<AssetStatus, Set<AssetStatus>> VALID_TRANSITIONS;

    static {
        VALID_TRANSITIONS = new EnumMap<>(AssetStatus.class);
        VALID_TRANSITIONS.put(AssetStatus.AVAILABLE,    EnumSet.of(AssetStatus.ASSIGNED, AssetStatus.UNDER_REPAIR, AssetStatus.RETIRED));
        VALID_TRANSITIONS.put(AssetStatus.ASSIGNED,     EnumSet.of(AssetStatus.AVAILABLE, AssetStatus.UNDER_REPAIR, AssetStatus.RECLAIMED, AssetStatus.LOST));
        VALID_TRANSITIONS.put(AssetStatus.UNDER_REPAIR, EnumSet.of(AssetStatus.AVAILABLE, AssetStatus.RETIRED));
        VALID_TRANSITIONS.put(AssetStatus.RECLAIMED,    EnumSet.of(AssetStatus.AVAILABLE, AssetStatus.RETIRED));
        VALID_TRANSITIONS.put(AssetStatus.LOST,         EnumSet.of(AssetStatus.RECLAIMED, AssetStatus.RETIRED));
        VALID_TRANSITIONS.put(AssetStatus.RETIRED,      EnumSet.noneOf(AssetStatus.class));
    }

    private final AssetRepository assetRepository;
    private final AssetHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AssetService(
            AssetRepository assetRepository,
            AssetHistoryRepository historyRepository,
            UserRepository userRepository,
            AuditService auditService,
            NotificationService notificationService
    ) {
        this.assetRepository = assetRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PagedResponse<AssetResponse> getAssets(AssetStatus status, String assetType, Long assignedUserId, Pageable pageable, String callerEmail) {
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + callerEmail));
        // EMPLOYEE can only see assets assigned to them
        Long effectiveAssignedUserId = caller.getRole().name().equals("EMPLOYEE")
                ? caller.getId()
                : assignedUserId;
        var page = assetRepository.findByFilters(status, assetType, effectiveAssignedUserId, pageable);
        return PagedResponse.from(page.map(AssetMapper::toResponse));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public AssetResponse getAsset(Long id) {
        return AssetMapper.toResponse(findById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Transactional
    public AssetResponse createAsset(AssetRequest request) {
        if (assetRepository.existsByAssetTag(request.assetTag())) {
            throw new DuplicateResourceException("Asset tag already exists: " + request.assetTag());
        }
        Asset asset = AssetMapper.toEntity(request);
        Asset saved = assetRepository.save(asset);
        writeHistory(saved, "CREATED", null, AssetStatus.AVAILABLE, null, null);
        auditService.log("ASSET", saved.getId().toString(), "CREATE", (Long) null, null, AssetMapper.toResponse(saved));
        log.info("Asset created: {}", saved.getAssetTag());
        return AssetMapper.toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Transactional
    public AssetResponse updateAsset(Long id, AssetRequest request) {
        Asset asset = findById(id);
        AssetResponse before = AssetMapper.toResponse(asset);
        AssetMapper.updateEntity(asset, request);
        Asset saved = assetRepository.save(asset);
        AssetResponse after = AssetMapper.toResponse(saved);
        auditService.log("ASSET", id.toString(), "UPDATE", (Long) null, before, after);
        return after;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Transactional
    public AssetResponse updateStatus(Long id, AssetStatusUpdateRequest request, String actorEmail) {
        Asset asset = findById(id);
        AssetStatus newStatus;
        try {
            newStatus = AssetStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown asset status: " + request.status());
        }

        validateTransition(asset.getStatus(), newStatus);

        AssetStatus oldStatus = asset.getStatus();
        asset.setStatus(newStatus);

        // Clear assignedUser when asset is no longer ASSIGNED
        if (newStatus != AssetStatus.ASSIGNED) {
            asset.setAssignedUser(null);
        }

        assetRepository.save(asset);

        User actor = userRepository.findByEmail(actorEmail).orElse(null);
        writeHistory(asset, "STATUS_CHANGE", oldStatus, newStatus, actor, request.notes());
        auditService.log("ASSET", id.toString(), "STATUS_CHANGE", actorEmail,
                Map.of("status", oldStatus.name()), Map.of("status", newStatus.name()));
        log.info("Asset {} status changed: {} → {}", asset.getAssetTag(), oldStatus, newStatus);

        return AssetMapper.toResponse(asset);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @Transactional
    public AssetResponse assignAsset(Long id, AssetAssignRequest request, String actorEmail) {
        Asset asset = findById(id);
        validateTransition(asset.getStatus(), AssetStatus.ASSIGNED);

        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        AssetStatus oldStatus = asset.getStatus();
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setAssignedUser(targetUser);
        assetRepository.save(asset);

        User actor = userRepository.findByEmail(actorEmail).orElse(null);
        writeHistory(asset, "ASSIGNED", oldStatus, AssetStatus.ASSIGNED, actor,
                request.notes() != null ? request.notes() : "Assigned to " + targetUser.getName());
        auditService.log("ASSET", id.toString(), "ASSIGN", actorEmail,
                Map.of("status", oldStatus.name()),
                Map.of("status", AssetStatus.ASSIGNED.name(), "assignedUserId", targetUser.getId()));
        notificationService.notify(targetUser.getId(),
                "Asset Assigned: " + asset.getAssetTag(),
                "Asset \"" + asset.getModel() + "\" has been assigned to you.",
                "ASSET", id.toString());
        log.info("Asset {} assigned to user {}", asset.getAssetTag(), targetUser.getEmail());

        return AssetMapper.toResponse(asset);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<AssetHistoryResponse> getHistory(Long id) {
        findById(id); // verify asset exists
        return historyRepository.findByAssetIdOrderByCreatedAtDesc(id)
                .stream()
                .map(AssetMapper::toHistoryResponse)
                .toList();
    }

    // --- helpers ---

    private Asset findById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
    }

    private void validateTransition(AssetStatus from, AssetStatus to) {
        Set<AssetStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(AssetStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStateTransitionException(from.name(), to.name());
        }
    }

    private void writeHistory(Asset asset, String eventType, AssetStatus from, AssetStatus to, User actor, String notes) {
        AssetHistory history = new AssetHistory();
        history.setAsset(asset);
        history.setEventType(eventType);
        history.setFromStatus(from != null ? from.name() : null);
        history.setToStatus(to != null ? to.name() : null);
        history.setActor(actor);
        history.setNotes(notes);
        historyRepository.save(history);
    }
}
