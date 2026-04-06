package com.company.portal.approval.repository;

import com.company.portal.approval.domain.ApprovalDecision;
import com.company.portal.approval.domain.ApprovalStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByRequestIdOrderBySequenceOrderAsc(Long requestId);

    Page<ApprovalStep> findByApproverIdAndDecision(Long approverId, ApprovalDecision decision, Pageable pageable);
}
