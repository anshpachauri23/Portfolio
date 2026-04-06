package com.company.portal.notification;

import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.notification.domain.Notification;
import com.company.portal.notification.repository.NotificationRepository;
import com.company.portal.notification.service.NotificationService;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);
    }

    private User testUser() {
        User u = new User();
        u.setId(1L);
        u.setName("Test User");
        u.setEmail("test@company.com");
        return u;
    }

    @Test
    void notify_byUserId_createsNotification() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser()));
        given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        notificationService.notify(1L, "Asset Assigned", "Laptop assigned to you", "ASSET", "5");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Asset Assigned");
        assertThat(saved.getBody()).isEqualTo("Laptop assigned to you");
        assertThat(saved.getEntityType()).isEqualTo("ASSET");
        assertThat(saved.getEntityId()).isEqualTo("5");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void notify_byEmail_resolvesUserAndCreates() {
        given(userRepository.findByEmail("test@company.com")).willReturn(Optional.of(testUser()));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser()));
        given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        notificationService.notify("test@company.com", "Request Approved", "Your request was approved", "SERVICE_REQUEST", "100");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void notify_unknownUser_throwsNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.notify(999L, "Title", "Body", null, null)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAsRead_setsReadToTrue() {
        Notification n = new Notification();
        n.setId(1L);
        n.setUser(testUser());
        n.setTitle("Test");
        n.setRead(false);

        given(notificationRepository.findById(1L)).willReturn(Optional.of(n));
        given(userRepository.findByEmail("test@company.com")).willReturn(Optional.of(testUser()));
        given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var result = notificationService.markAsRead(1L, "test@company.com");

        assertThat(result.read()).isTrue();
    }

    @Test
    void markAllAsRead_callsRepository() {
        given(userRepository.findByEmail("test@company.com")).willReturn(Optional.of(testUser()));
        given(notificationRepository.markAllAsRead(1L)).willReturn(5);

        notificationService.markAllAsRead("test@company.com");

        verify(notificationRepository).markAllAsRead(1L);
    }
}
