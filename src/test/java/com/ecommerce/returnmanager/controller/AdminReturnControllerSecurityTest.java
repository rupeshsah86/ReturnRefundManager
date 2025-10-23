package com.ecommerce.returnmanager.controller;

import com.ecommerce.returnmanager.service.ReturnRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminReturnController.class)
class AdminReturnControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReturnRequestService returnRequestService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPendingReturns_WithAdminRole_ShouldReturnOk() throws Exception {
        when(returnRequestService.getAllPendingRequests()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/v1/admin/returns/pending"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllPendingReturns_WithCustomerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/returns/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPendingReturns_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/returns/pending"))
                .andExpect(status().isUnauthorized());
    }
}