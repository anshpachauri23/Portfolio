package com.company.portal.notification.service;

import com.company.portal.common.dto.PagedResponse;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.notification.domain.Notification;
import com.company.portal.notification.dto.NotificationResponse;
import com.company.portal.notification.repository.NotificationRepository;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notify(Long userId, String title, String body, String entityType, String entityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setBody(body);
        n.setEntityType(entityType);
        n.setEntityId(entityId);
        notificationRepository.save(n);
        log.debug("Notification sent to user {}: {}", userId, title);
    }

    @Transactional
    public void notify(String userEmail, String title, String body, String entityType, String entityId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        notify(user.getId(), title, body, entityType, entityId);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        return PagedResponse.from(
                notificationRepository.findByUserId(user.getId(), pageable)
                        .map(this::toResponse)
        );
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User user = findUserByEmail(userEmail);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userEmail) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        User user = findUserByEmail(userEmail);
        if (!n.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification", notificationId);
        }
        n.setRead(true);
        notificationRepository.save(n);
        return toResponse(n);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = findUserByEmail(userEmail);
        int count = notificationRepository.markAllAsRead(user.getId());
        log.debug("Marked {} notifications as read for user {}", count, userEmail);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.isRead(),
                n.getEntityType(),
                n.getEntityId(),
                n.getCreatedAt()
        );
    }
}
