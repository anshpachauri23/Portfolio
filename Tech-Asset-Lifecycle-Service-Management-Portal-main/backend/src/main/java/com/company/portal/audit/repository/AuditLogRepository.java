package com.company.portal.audit.repository;

import com.company.portal.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE "
            + "(:entityType IS NULL OR a.entityType = :entityType) AND "
            + "(:actorId IS NULL OR a.actor.id = :actorId) "
            + "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFilters(
            @Param("entityType") String entityType,
            @Param("actorId") Long actorId,
            Pageable pageable
    );
}
