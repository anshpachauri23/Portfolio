package com.company.portal.attachment.controller;

import com.company.portal.attachment.dto.AttachmentResponse;
import com.company.portal.attachment.service.AttachmentService;
import com.company.portal.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Attachments", description = "File attachment management")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/requests/{id}/attachments")
    @Operation(summary = "Upload a file attachment to a service request")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadToRequest(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                attachmentService.upload("SERVICE_REQUEST", id.toString(), file, callerEmail),
                "File uploaded"
        ));
    }

    @GetMapping("/requests/{id}/attachments")
    @Operation(summary = "List attachments for a service request")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> listRequestAttachments(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                attachmentService.listAttachments("SERVICE_REQUEST", id.toString()),
                "Attachments retrieved"
        ));
    }

    @GetMapping("/attachments/{id}/download")
    @Operation(summary = "Download an attachment")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        AttachmentService.DownloadResult result = attachmentService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        result.contentType() != null ? result.contentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\"")
                .body(result.resource());
    }
}
