package com.company.portal.auth;

import com.company.portal.auth.controller.AuthController;
import com.company.portal.auth.dto.LoginRequest;
import com.company.portal.auth.dto.LoginResponse;
import com.company.portal.auth.dto.UserProfileResponse;
import com.company.portal.auth.security.JwtAuthenticationEntryPoint;
import com.company.portal.auth.security.JwtAuthenticationFilter;
import com.company.portal.auth.security.JwtUtil;
import com.company.portal.auth.service.AuthService;
import com.company.portal.common.config.AppConfig;
import com.company.portal.common.config.SecurityConfig;
import com.company.portal.auth.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AppConfig.class, JwtAuthenticationFilter.class,
         JwtAuthenticationEntryPoint.class, JwtUtil.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loginSuccess_returns200WithToken() throws Exception {
        var request = new LoginRequest("alice@company.com", "Password1!");
        var response = new LoginResponse("jwt.token.here", 1L, "Alice Admin", "alice@company.com", "ADMIN");

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void loginWithBadCredentials_returns401() throws Exception {
        var request = new LoginRequest("alice@company.com", "wrongpassword");

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithMissingFields_returns400() throws Exception {
        var invalidRequest = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_withValidToken_returns200() throws Exception {
        var profile = new UserProfileResponse(1L, "Alice Admin", "alice@company.com", "EMP001", "ADMIN", 1L, "Engineering");
        given(authService.getCurrentUser("alice@company.com")).willReturn(profile);

        mockMvc.perform(get("/api/auth/me")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("alice@company.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("alice@company.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }
}
