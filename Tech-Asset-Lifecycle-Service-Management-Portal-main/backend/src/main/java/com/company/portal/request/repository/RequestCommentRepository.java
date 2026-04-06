package com.company.portal.request.repository;

import com.company.portal.request.domain.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {

    List<RequestComment> findByServiceRequestIdOrderByCreatedAtAsc(Long requestId);
}
