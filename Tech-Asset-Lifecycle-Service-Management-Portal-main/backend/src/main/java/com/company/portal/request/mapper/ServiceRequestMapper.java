package com.company.portal.request.mapper;

import com.company.portal.request.domain.RequestComment;
import com.company.portal.request.domain.RequestType;
import com.company.portal.request.domain.ServiceRequest;
import com.company.portal.request.dto.RequestCommentResponse;
import com.company.portal.request.dto.RequestTypeResponse;
import com.company.portal.request.dto.ServiceRequestResponse;

import java.util.List;

public final class ServiceRequestMapper {

    private ServiceRequestMapper() {}

    public static ServiceRequestResponse toResponse(ServiceRequest sr, List<RequestComment> comments) {
        return new ServiceRequestResponse(
                sr.getId(),
                sr.getRequestNumber(),
                sr.getRequestType().getId(),
                sr.getRequestType().getName(),
                sr.getTitle(),
                sr.getDescription(),
                sr.getRequester().getId(),
                sr.getRequester().getName(),
                sr.getAsset() != null ? sr.getAsset().getId() : null,
                sr.getPriority(),
                sr.getStatus().name(),
                sr.getAssignedTo() != null ? sr.getAssignedTo().getId() : null,
                sr.getAssignedTo() != null ? sr.getAssignedTo().getName() : null,
                sr.isApprovalRequired(),
                sr.getDueDate(),
                sr.getClosedAt(),
                sr.getCreatedAt(),
                sr.getUpdatedAt(),
                comments.stream().map(ServiceRequestMapper::toCommentResponse).toList()
        );
    }

    public static RequestCommentResponse toCommentResponse(RequestComment comment) {
        return new RequestCommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }

    public static RequestTypeResponse toTypeResponse(RequestType rt) {
        return new RequestTypeResponse(rt.getId(), rt.getName(), rt.getDescription(), rt.isApprovalRequired(), rt.isActive());
    }
}
