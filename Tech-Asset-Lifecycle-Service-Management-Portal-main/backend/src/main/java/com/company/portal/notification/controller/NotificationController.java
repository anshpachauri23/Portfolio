package com.company.portal.notification.controller;

import com.company.portal.common.dto.ApiResponse;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.notification.dto.NotificationResponse;
import com.company.portal.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notification management")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get current user's notifications (unread first)")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal String callerEmail,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                notificationService.getNotifications(callerEmail, pageable),
                "Notifications retrieved"
        ));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                notificationService.getUnreadCount(callerEmail),
                "Unread count retrieved"
        ));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal String callerEmail
    ) {
        return ResponseEntity.ok(ApiResponse.of(
                notificationService.markAsRead(id, callerEmail),
                "Notification marked as read"
        ));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal String callerEmail
    ) {
        notificationService.markAllAsRead(callerEmail);
        return ResponseEntity.ok(ApiResponse.of(null, "All notifications marked as read"));
    }
}
