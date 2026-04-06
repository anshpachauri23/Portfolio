package com.company.portal.request.controller;

import com.company.portal.common.dto.ApiResponse;
import com.company.portal.request.dto.RequestTypeResponse;
import com.company.portal.request.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/request-types")
@Tag(name = "Request Types", description = "Active request type lookup")
public class RequestTypeController {

    private final ServiceRequestService requestService;

    public RequestTypeController(ServiceRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    @Operation(summary = "List all active request types")
    public ResponseEntity<ApiResponse<List<RequestTypeResponse>>> getActiveRequestTypes() {
        return ResponseEntity.ok(ApiResponse.of(requestService.getActiveRequestTypes(), "Request types retrieved"));
    }
}
