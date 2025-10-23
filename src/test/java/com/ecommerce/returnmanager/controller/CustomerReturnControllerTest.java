package com.ecommerce.returnmanager.controller;

import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.User;
import com.ecommerce.returnmanager.service.ReturnRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerReturnController.class)
class CustomerReturnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReturnRequestService returnRequestService;

    @MockBean
    private com.ecommerce.returnmanager.repository.UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void getReturnDetails_WithValidId_ShouldReturnRequest() throws Exception {
        // Create a complete ReturnRequest with User
        User testUser = new User(1L, "Test Customer", "customer@test.com", "password", User.Role.CUSTOMER);
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(1L);
        returnRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
        returnRequest.setUser(testUser); // Set the user to avoid NPE

        when(returnRequestService.getRequestById(1L)).thenReturn(Optional.of(returnRequest));
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/v1/customer/returns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void getReturnDetails_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(returnRequestService.getRequestById(999L)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(get("/api/v1/customer/returns/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void getReturnDetails_WithUnauthorizedUser_ShouldReturnForbidden() throws Exception {
        // Simulate user trying to access someone else's return
        User differentUser = new User(2L, "Different User", "different@test.com", "password", User.Role.CUSTOMER);
        User currentUser = new User(1L, "Current User", "customer@test.com", "password", User.Role.CUSTOMER);
        
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setId(1L);
        returnRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
        returnRequest.setUser(differentUser); // Return belongs to different user

        when(returnRequestService.getRequestById(1L)).thenReturn(Optional.of(returnRequest));
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(currentUser));

        mockMvc.perform(get("/api/v1/customer/returns/1"))
                .andExpect(status().isForbidden());
    }
}