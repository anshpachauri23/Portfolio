package com.company.portal.admin.service;

import com.company.portal.admin.dto.UserCreateRequest;
import com.company.portal.admin.dto.UserResponse;
import com.company.portal.admin.dto.UserUpdateRequest;
import com.company.portal.audit.service.AuditService;
import com.company.portal.auth.domain.Role;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.common.exception.DuplicateResourceException;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.company.portal.user.domain.Department;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.DepartmentRepository;
import com.company.portal.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdminUserService(UserRepository userRepository,
                            DepartmentRepository departmentRepository,
                            PasswordEncoder passwordEncoder,
                            AuditService auditService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listUsers(Pageable pageable) {
        return PagedResponse.from(userRepository.findAll(pageable).map(this::toResponse));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return toResponse(findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse createUser(UserCreateRequest dto, String actorEmail) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("Email already exists: " + dto.email());
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setEmployeeCode(dto.employeeCode());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(Role.valueOf(dto.role()));
        user.setDepartment(resolveDepartment(dto.departmentId()));
        user.setManager(resolveManager(dto.managerId()));

        User saved = userRepository.save(user);
        auditService.log("USER", saved.getId().toString(), "CREATE", actorEmail, null, toResponse(saved));
        log.info("User created: {}", saved.getEmail());
        return toResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest dto, String actorEmail) {
        User user = findById(id);
        UserResponse before = toResponse(user);

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setEmployeeCode(dto.employeeCode());
        if (dto.role() != null) {
            user.setRole(Role.valueOf(dto.role()));
        }
        user.setDepartment(resolveDepartment(dto.departmentId()));
        user.setManager(resolveManager(dto.managerId()));

        User saved = userRepository.save(user);
        UserResponse after = toResponse(saved);
        auditService.log("USER", id.toString(), "UPDATE", actorEmail, before, after);
        log.info("User updated: {}", saved.getEmail());
        return after;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse deactivateUser(Long id, String actorEmail) {
        User user = findById(id);
        user.setActive(false);
        User saved = userRepository.save(user);
        auditService.log("USER", id.toString(), "DEACTIVATE", actorEmail, null, null);
        log.info("User deactivated: {}", saved.getEmail());
        return toResponse(saved);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", departmentId));
    }

    private User resolveManager(Long managerId) {
        if (managerId == null) return null;
        return userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager user", managerId));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getEmployeeCode(),
                u.getRole().name(),
                u.getDepartment() != null ? u.getDepartment().getId() : null,
                u.getDepartment() != null ? u.getDepartment().getName() : null,
                u.getManager() != null ? u.getManager().getId() : null,
                u.getManager() != null ? u.getManager().getName() : null,
                u.isActive(),
                u.getCreatedAt()
        );
    }
}
