package com.company.portal.admin;

import com.company.portal.admin.dto.UserCreateRequest;
import com.company.portal.admin.dto.UserResponse;
import com.company.portal.admin.dto.UserUpdateRequest;
import com.company.portal.admin.service.AdminUserService;
import com.company.portal.audit.service.AuditService;
import com.company.portal.auth.domain.Role;
import com.company.portal.common.exception.DuplicateResourceException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.DepartmentRepository;
import com.company.portal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private AuditService auditService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userRepository, departmentRepository, passwordEncoder, auditService);
    }

    private User savedUser(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setName("Test User");
        u.setEmail(email);
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_newEmail_savesAndAudits() {
        given(userRepository.existsByEmail("new@company.com")).willReturn(false);
        given(userRepository.save(any())).willAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        var request = new UserCreateRequest("New User", "new@company.com", "EMP001", "Password1!", "EMPLOYEE", null, null);
        UserResponse result = adminUserService.createUser(request, "admin@company.com");

        assertThat(result.email()).isEqualTo("new@company.com");
        assertThat(result.role()).isEqualTo("EMPLOYEE");
        assertThat(result.active()).isTrue();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(passwordEncoder.matches("Password1!", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_duplicateEmail_throwsDuplicate() {
        given(userRepository.existsByEmail("existing@company.com")).willReturn(true);

        var request = new UserCreateRequest("Someone", "existing@company.com", null, "pass", "EMPLOYEE", null, null);
        assertThatThrownBy(() -> adminUserService.createUser(request, "admin@company.com"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("existing@company.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_changesNameAndRole() {
        User existing = savedUser(5L, "user@company.com", Role.EMPLOYEE);
        given(userRepository.findById(5L)).willReturn(Optional.of(existing));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var request = new UserUpdateRequest("Updated Name", "user@company.com", null, "TECHNICIAN", null, null);
        UserResponse result = adminUserService.updateUser(5L, request, "admin@company.com");

        assertThat(result.name()).isEqualTo("Updated Name");
        assertThat(result.role()).isEqualTo("TECHNICIAN");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_notFound_throwsNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        var request = new UserUpdateRequest("Name", "email@x.com", null, null, null, null);
        assertThatThrownBy(() -> adminUserService.updateUser(999L, request, "admin@company.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_setsActiveFalse() {
        User user = savedUser(7L, "user@company.com", Role.EMPLOYEE);
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UserResponse result = adminUserService.deactivateUser(7L, "admin@company.com");

        assertThat(result.active()).isFalse();
        verify(auditService).log(any(), any(), any(), any(String.class), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_auditsWithCreateAction() {
        given(userRepository.existsByEmail("audited@company.com")).willReturn(false);
        given(userRepository.save(any())).willAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(20L);
            return u;
        });

        adminUserService.createUser(
                new UserCreateRequest("Audit Test", "audited@company.com", null, "pass", "EMPLOYEE", null, null),
                "admin@company.com");

        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(any(), any(), actionCaptor.capture(), any(String.class), any(), any());
        assertThat(actionCaptor.getValue()).isEqualTo("CREATE");
    }
}
