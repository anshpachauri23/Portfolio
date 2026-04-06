package com.company.portal.admin.service;

import com.company.portal.admin.dto.RequestTypeRequest;
import com.company.portal.admin.dto.RequestTypeResponse;
import com.company.portal.audit.service.AuditService;
import com.company.portal.common.exception.DuplicateResourceException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.request.domain.RequestType;
import com.company.portal.request.repository.RequestTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminRequestTypeService {

    private static final Logger log = LoggerFactory.getLogger(AdminRequestTypeService.class);

    private final RequestTypeRepository requestTypeRepository;
    private final AuditService auditService;

    public AdminRequestTypeService(RequestTypeRepository requestTypeRepository, AuditService auditService) {
        this.requestTypeRepository = requestTypeRepository;
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<RequestTypeResponse> listAll() {
        return requestTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RequestTypeResponse create(RequestTypeRequest dto, String actorEmail) {
        if (requestTypeRepository.findAll().stream().anyMatch(rt -> rt.getName().equalsIgnoreCase(dto.name()))) {
            throw new DuplicateResourceException("Request type already exists: " + dto.name());
        }

        RequestType rt = new RequestType();
        rt.setName(dto.name());
        rt.setDescription(dto.description());
        rt.setApprovalRequired(dto.approvalRequired());

        RequestType saved = requestTypeRepository.save(rt);
        auditService.log("REQUEST_TYPE", saved.getId().toString(), "CREATE", actorEmail, null, toResponse(saved));
        log.info("Request type created: {}", saved.getName());
        return toResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RequestTypeResponse update(Long id, RequestTypeRequest dto, String actorEmail) {
        RequestType rt = requestTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RequestType", id));

        RequestTypeResponse before = toResponse(rt);
        rt.setName(dto.name());
        rt.setDescription(dto.description());
        rt.setApprovalRequired(dto.approvalRequired());

        RequestType saved = requestTypeRepository.save(rt);
        RequestTypeResponse after = toResponse(saved);
        auditService.log("REQUEST_TYPE", id.toString(), "UPDATE", actorEmail, before, after);
        log.info("Request type updated: {}", saved.getName());
        return after;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RequestTypeResponse toggleActive(Long id, String actorEmail) {
        RequestType rt = requestTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RequestType", id));

        rt.setActive(!rt.isActive());
        RequestType saved = requestTypeRepository.save(rt);
        auditService.log("REQUEST_TYPE", id.toString(), rt.isActive() ? "ACTIVATE" : "DEACTIVATE",
                actorEmail, null, null);
        log.info("Request type {} {}", saved.getName(), saved.isActive() ? "activated" : "deactivated");
        return toResponse(saved);
    }

    private RequestTypeResponse toResponse(RequestType rt) {
        return new RequestTypeResponse(
                rt.getId(),
                rt.getName(),
                rt.getDescription(),
                rt.isApprovalRequired(),
                rt.isActive(),
                rt.getCreatedAt()
        );
    }
}
