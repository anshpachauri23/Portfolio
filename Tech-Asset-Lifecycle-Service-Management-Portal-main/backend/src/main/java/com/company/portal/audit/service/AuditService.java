package com.company.portal.audit.service;

import com.company.portal.audit.domain.AuditLog;
import com.company.portal.audit.dto.AuditLogResponse;
import com.company.portal.audit.repository.AuditLogRepository;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, String entityId, String action, Long actorId, Object before, Object after) {
        AuditLog entry = new AuditLog();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setAction(action);

        if (actorId != null) {
            userRepository.findById(actorId).ifPresent(entry::setActor);
        }

        entry.setBeforeJson(toJson(before));
        entry.setAfterJson(toJson(after));

        auditLogRepository.save(entry);
        log.debug("Audit: {} {} {} by actor {}", action, entityType, entityId, actorId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, String entityId, String action, String actorEmail, Object before, Object after) {
        Long actorId = null;
        if (actorEmail != null) {
            actorId = userRepository.findByEmail(actorEmail).map(User::getId).orElse(null);
        }
        log(entityType, entityId, action, actorId, before, after);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(String entityType, Long actorId, Pageable pageable) {
        return PagedResponse.from(
                auditLogRepository.findByFilters(entityType, actorId, pageable)
                        .map(this::toResponse)
        );
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getAction(),
                entry.getActor() != null ? entry.getActor().getId() : null,
                entry.getActor() != null ? entry.getActor().getName() : null,
                entry.getBeforeJson(),
                entry.getAfterJson(),
                entry.getCreatedAt()
        );
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize audit data: {}", e.getMessage());
            return null;
        }
    }
}
