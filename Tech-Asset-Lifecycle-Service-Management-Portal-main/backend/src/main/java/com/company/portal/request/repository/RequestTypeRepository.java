package com.company.portal.request.repository;

import com.company.portal.request.domain.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {

    List<RequestType> findByActiveTrue();
}
