package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.technicianProfile.TechnicianProfileResponseDTO;
import com.smartresidential.backend.repositories.SessionRepository;
import com.smartresidential.backend.repositories.TenantRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.JwtService;
import com.smartresidential.backend.services.interfaces.TechnicianProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TechnicianProfileController.class)
class TechnicianProfileControllerJsonTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TechnicianProfileService technicianProfileService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SessionRepository sessionRepository;

    @MockitoBean
    private TenantRepository tenantRepository;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getTechniciansReturnsWorkloadMetadataFieldNames() throws Exception {
        when(technicianProfileService.getAll()).thenReturn(List.of(technician(false)));

        mockMvc.perform(get("/api/technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].userId").value(20))
                .andExpect(jsonPath("$[0].specialization").value("Plumbing"))
                .andExpect(jsonPath("$[0].isAvailable").value(false))
                .andExpect(jsonPath("$[0].activeIssueCount").value(1))
                .andExpect(jsonPath("$[0].activeHighPriorityIssueCount").value(1))
                .andExpect(jsonPath("$[0].maxActiveIssues").value(5))
                .andExpect(jsonPath("$[0].lastAssignedAt").value("2026-05-18T10:30:00"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAvailableTechniciansReturnsWorkloadMetadataFieldNames() throws Exception {
        when(technicianProfileService.getAvailable()).thenReturn(List.of(technician(true)));

        mockMvc.perform(get("/api/technicians/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isAvailable").value(true))
                .andExpect(jsonPath("$[0].activeIssueCount").value(1))
                .andExpect(jsonPath("$[0].activeHighPriorityIssueCount").value(1))
                .andExpect(jsonPath("$[0].maxActiveIssues").value(5))
                .andExpect(jsonPath("$[0].lastAssignedAt").value("2026-05-18T10:30:00"));
    }

    private TechnicianProfileResponseDTO technician(boolean available) {
        TechnicianProfileResponseDTO technician = new TechnicianProfileResponseDTO();
        technician.setId(50L);
        technician.setUserId(20L);
        technician.setSpecialization("Plumbing");
        technician.setIsAvailable(available);
        technician.setActiveIssueCount(1);
        technician.setActiveHighPriorityIssueCount(1);
        technician.setMaxActiveIssues(5);
        technician.setLastAssignedAt(LocalDateTime.of(2026, 5, 18, 10, 30));
        return technician;
    }
}
