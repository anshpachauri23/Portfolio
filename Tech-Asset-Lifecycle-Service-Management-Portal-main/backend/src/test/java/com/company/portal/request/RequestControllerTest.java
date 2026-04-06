package com.company.portal.request;

import com.company.portal.auth.security.JwtAuthenticationEntryPoint;
import com.company.portal.auth.security.JwtAuthenticationFilter;
import com.company.portal.auth.security.JwtUtil;
import com.company.portal.auth.service.CustomUserDetailsService;
import com.company.portal.common.config.AppConfig;
import com.company.portal.common.config.SecurityConfig;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.request.controller.ServiceRequestController;
import com.company.portal.request.dto.ServiceRequestResponse;
import com.company.portal.request.service.ServiceRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceRequestController.class)
@Import({SecurityConfig.class, AppConfig.class, JwtAuthenticationFilter.class,
         JwtAuthenticationEntryPoint.class, JwtUtil.class})
class RequestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ServiceRequestService requestService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private ServiceRequestResponse sampleResponse(String requestNumber, String status) {
        return new ServiceRequestResponse(1L, requestNumber, 1L, "New Device", "Need laptop",
                "I need a new laptop", 1L, "Emma Employee", null, "MEDIUM", status,
                null, null, false, null, null,
                java.time.Instant.now(), java.time.Instant.now(), List.of());
    }

    @Test
    @WithMockUser(username = "emma@company.com", roles = "EMPLOYEE")
    void getMyRequests_returnsOwnRequests() throws Exception {
        var paged = new PagedResponse<>(List.of(sampleResponse("SR-20260403-01000", "IN_PROGRESS")), 0, 20, 1L, 1);
        given(requestService.getMyRequests(any(), any())).willReturn(paged);

        mockMvc.perform(get("/api/requests/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].requestNumber").value("SR-20260403-01000"));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void getQueue_asTechnician_returns200() throws Exception {
        var paged = new PagedResponse<>(List.of(sampleResponse("SR-20260403-01001", "PENDING_APPROVAL")), 0, 20, 1L, 1);
        given(requestService.getQueue(any(), any())).willReturn(paged);

        mockMvc.perform(get("/api/requests/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING_APPROVAL"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getQueue_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/requests/queue"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyRequests_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/requests/my"))
                .andExpect(status().isUnauthorized());
    }
}
