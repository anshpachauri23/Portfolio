package com.company.portal.asset;

import com.company.portal.asset.controller.AssetController;
import com.company.portal.asset.dto.*;
import com.company.portal.asset.service.AssetService;
import com.company.portal.auth.security.JwtAuthenticationEntryPoint;
import com.company.portal.auth.security.JwtAuthenticationFilter;
import com.company.portal.auth.security.JwtUtil;
import com.company.portal.auth.service.CustomUserDetailsService;
import com.company.portal.common.config.AppConfig;
import com.company.portal.common.config.SecurityConfig;
import com.company.portal.common.dto.PagedResponse;
import com.company.portal.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
@Import({SecurityConfig.class, AppConfig.class, JwtAuthenticationFilter.class,
         JwtAuthenticationEntryPoint.class, JwtUtil.class})
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private AssetResponse sampleResponse() {
        return new AssetResponse(1L, "TAG-001", null, "LAPTOP", "Dell", "XPS 15",
                "AVAILABLE", null, null, null, null, null, null, null,
                Instant.now(), Instant.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAssets_returns200WithPagedResponse() throws Exception {
        var paged = new PagedResponse<>(List.of(sampleResponse()), 0, 20, 1L, 1);
        given(assetService.getAssets(any(), any(), any(), any(), any())).willReturn(paged);

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].assetTag").value("TAG-001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAsset_exists_returns200() throws Exception {
        given(assetService.getAsset(1L)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetTag").value("TAG-001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAsset_notFound_returns404() throws Exception {
        given(assetService.getAsset(999L)).willThrow(new ResourceNotFoundException("Asset", 999L));

        mockMvc.perform(get("/api/assets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAsset_validRequest_returns201() throws Exception {
        var request = new AssetRequest("TAG-002", null, "MONITOR", "LG", "27UL550", null, null, null, null, null);
        var response = new AssetResponse(2L, "TAG-002", null, "MONITOR", "LG", "27UL550",
                "AVAILABLE", null, null, null, null, null, null, null,
                Instant.now(), Instant.now());

        given(assetService.createAsset(any())).willReturn(response);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.assetTag").value("TAG-002"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createAsset_asEmployee_returns403() throws Exception {
        var request = new AssetRequest("TAG-002", null, "MONITOR", "LG", "27UL550", null, null, null, null, null);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAsset_withoutAuthentication_returns401() throws Exception {
        var request = new AssetRequest("TAG-002", null, "MONITOR", "LG", "27UL550", null, null, null, null, null);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
