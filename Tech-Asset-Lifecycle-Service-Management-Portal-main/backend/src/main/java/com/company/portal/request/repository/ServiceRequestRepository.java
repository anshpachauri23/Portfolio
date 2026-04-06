package com.company.portal.request.repository;

import com.company.portal.request.domain.RequestStatus;
import com.company.portal.request.domain.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    Page<ServiceRequest> findByRequesterId(Long requesterId, Pageable pageable);

    Page<ServiceRequest> findByStatusIn(List<RequestStatus> statuses, Pageable pageable);

    @Query(value = "SELECT nextval('service_request_seq')", nativeQuery = true)
    Long nextRequestSequence();
}
