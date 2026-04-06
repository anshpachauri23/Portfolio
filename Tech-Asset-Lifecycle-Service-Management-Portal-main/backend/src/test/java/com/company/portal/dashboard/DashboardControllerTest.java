package com.company.portal.dashboard;

import com.company.portal.auth.security.JwtAuthenticationEntryPoint;
import com.company.portal.auth.security.JwtAuthenticationFilter;
import com.company.portal.auth.security.JwtUtil;
import com.company.portal.auth.service.CustomUserDetailsService;
import com.company.portal.common.config.AppConfig;
import com.company.portal.common.config.SecurityConfig;
import com.company.portal.dashboard.controller.DashboardController;
import com.company.portal.dashboard.dto.*;
import com.company.portal.dashboard.service.DashboardService;
import com.company.portal.user.domain.User;
import com.company.portal.user.repository.UserRepository;
import com.company.portal.auth.domain.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, AppConfig.class, JwtAuthenticationFilter.class,
         JwtAuthenticationEntryPoint.class, JwtUtil.class})
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DashboardService dashboardService;
    @MockBean private UserRepository userRepository;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User userWithRole(Role role, String email) {
        User u = new User();
        u.setId(1L);
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getSummary_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getRequestTrends_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/request-trends"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getSlaAging_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/sla-aging"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@company.com", roles = "ADMIN")
    void getSummary_asAdmin_returns200WithCorrectShape() throws Exception {
        given(userRepository.findByEmail("admin@company.com"))
                .willReturn(Optional.of(userWithRole(Role.ADMIN, "admin@company.com")));
        given(dashboardService.getSummary(null)).willReturn(
                new DashboardSummary(
                        Map.of("IN_PROGRESS", 5L, "PENDING_APPROVAL", 2L),
                        Map.of("AVAILABLE", 10L, "ASSIGNED", 8L)
                )
        );

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestCountByStatus.IN_PROGRESS").value(5))
                .andExpect(jsonPath("$.data.assetCountByStatus.AVAILABLE").value(10));
    }

    @Test
    @WithMockUser(username = "mike@company.com", roles = "MANAGER")
    void getSummary_asManager_passesManagerScopeToService() throws Exception {
        User manager = userWithRole(Role.MANAGER, "mike@company.com");
        manager.setId(42L);
        given(userRepository.findByEmail("mike@company.com")).willReturn(Optional.of(manager));
        given(dashboardService.getSummary(42L)).willReturn(
                new DashboardSummary(Map.of("IN_PROGRESS", 1L), Map.of())
        );

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestCountByStatus.IN_PROGRESS").value(1));
    }

    @Test
    @WithMockUser(username = "tina@company.com", roles = "TECHNICIAN")
    void getRequestTrends_asTechnician_returns200() throws Exception {
        given(userRepository.findByEmail("tina@company.com"))
                .willReturn(Optional.of(userWithRole(Role.TECHNICIAN, "tina@company.com")));
        given(dashboardService.getRequestTrends(null)).willReturn(List.of(
                new RequestTrend("2026-01", 3),
                new RequestTrend("2026-02", 7)
        ));

        mockMvc.perform(get("/api/dashboard/request-trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].month").value("2026-01"))
                .andExpect(jsonPath("$.data[0].count").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAssetsByStatus_returns200() throws Exception {
        given(dashboardService.getAssetsByStatus()).willReturn(List.of(
                new StatusCount("AVAILABLE", 10),
                new StatusCount("ASSIGNED", 5)
        ));

        mockMvc.perform(get("/api/dashboard/assets-by-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data[0].count").value(10));
    }

    @Test
    void getSummary_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }
}
