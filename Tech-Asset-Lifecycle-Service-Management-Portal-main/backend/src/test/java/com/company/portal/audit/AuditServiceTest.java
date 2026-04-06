package com.company.portal.audit;

import com.company.portal.audit.domain.AuditLog;
import com.company.portal.audit.repository.AuditLogRepository;
import com.company.portal.audit.service.AuditService;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private UserRepository userRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository, userRepository, new ObjectMapper());
    }

    @Test
    void log_byActorId_savesCorrectFields() {
        User actor = new User();
        actor.setId(5L);
        actor.setName("Admin User");
        given(userRepository.findById(5L)).willReturn(Optional.of(actor));
        given(auditLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        auditService.log("ASSET", "1", "CREATE", 5L, null, Map.of("status", "AVAILABLE"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo("ASSET");
        assertThat(saved.getEntityId()).isEqualTo("1");
        assertThat(saved.getAction()).isEqualTo("CREATE");
        assertThat(saved.getActor().getId()).isEqualTo(5L);
        assertThat(saved.getBeforeJson()).isNull();
        assertThat(saved.getAfterJson()).contains("AVAILABLE");
    }

    @Test
    void log_byActorEmail_resolvesIdAndSaves() {
        User actor = new User();
        actor.setId(5L);
        given(userRepository.findByEmail("admin@company.com")).willReturn(Optional.of(actor));
        given(userRepository.findById(5L)).willReturn(Optional.of(actor));
        given(auditLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        auditService.log("SERVICE_REQUEST", "100", "STATUS_CHANGE", "admin@company.com",
                Map.of("status", "SUBMITTED"), Map.of("status", "IN_PROGRESS"));

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void log_nullActor_savesWithoutActor() {
        given(auditLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        auditService.log("ASSET", "2", "UPDATE", (Long) null, "before", "after");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getActor()).isNull();
    }

    @Test
    void log_withStringPayload_storesDirectly() {
        given(auditLogRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        auditService.log("USER", "3", "DEACTIVATE", (Long) null, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getBeforeJson()).isNull();
        assertThat(captor.getValue().getAfterJson()).isNull();
    }
}
