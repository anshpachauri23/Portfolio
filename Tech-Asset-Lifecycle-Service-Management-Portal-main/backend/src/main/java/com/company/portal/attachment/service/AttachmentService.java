package com.company.portal.attachment.service;

import com.company.portal.attachment.domain.Attachment;
import com.company.portal.attachment.dto.AttachmentResponse;
import com.company.portal.attachment.repository.AttachmentRepository;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             StorageService storageService,
                             UserRepository userRepository) {
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AttachmentResponse upload(String entityType, String entityId, MultipartFile file, String uploaderEmail) {
        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uploaderEmail));

        String storageKey;
        try {
            storageKey = storageService.store(file, entityType, entityId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }

        Attachment attachment = new Attachment();
        attachment.setEntityType(entityType);
        attachment.setEntityId(entityId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setStorageKey(storageKey);
        attachment.setUploadedBy(uploader);

        Attachment saved = attachmentRepository.save(attachment);
        log.info("File uploaded: {} for {} {}", file.getOriginalFilename(), entityType, entityId);
        return toResponse(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<AttachmentResponse> listAttachments(String entityType, String entityId) {
        return attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public DownloadResult download(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
        try {
            Resource resource = storageService.load(attachment.getStorageKey());
            return new DownloadResult(resource, attachment.getFileName(), attachment.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + e.getMessage(), e);
        }
    }

    private AttachmentResponse toResponse(Attachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getFileName(),
                a.getFileSize(),
                a.getContentType(),
                a.getUploadedBy().getId(),
                a.getUploadedBy().getName(),
                a.getCreatedAt()
        );
    }

    public record DownloadResult(Resource resource, String fileName, String contentType) {}
}
