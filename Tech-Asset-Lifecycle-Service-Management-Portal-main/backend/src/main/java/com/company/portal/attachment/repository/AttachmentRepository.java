package com.company.portal.attachment.repository;

import com.company.portal.attachment.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
