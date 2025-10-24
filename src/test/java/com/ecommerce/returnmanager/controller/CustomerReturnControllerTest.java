// package com.ecommerce.returnmanager.controller;

// import com.ecommerce.returnmanager.model.ReturnRequest;
// import com.ecommerce.returnmanager.model.User;
// import com.ecommerce.returnmanager.service.ReturnRequestService;
// import com.ecommerce.returnmanager.repository.UserRepository;
// import com.ecommerce.returnmanager.repository.ReturnRequestRepository; // Add this import
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(CustomerReturnController.class)
// class CustomerReturnControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private ReturnRequestService returnRequestService;

//     @MockBean
//     private UserRepository userRepository;

//     @MockBean
//     private ReturnRequestRepository returnRequestRepository; // ADD THIS LINE

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Test
//     @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
//     void getReturnDetails_WithValidId_ShouldReturnRequest() throws Exception {
//         User testUser = new User(1L, "Test Customer", "customer@test.com", "password", User.Role.CUSTOMER);
//         ReturnRequest returnRequest = new ReturnRequest();
//         returnRequest.setId(1L);
//         returnRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
//         returnRequest.setUser(testUser);

//         when(returnRequestService.getRequestById(1L)).thenReturn(Optional.of(returnRequest));
//         when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));

//         mockMvc.perform(get("/api/v1/customer/returns/1"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.success").value(true))
//                 .andExpect(jsonPath("$.data.id").value(1L))
//                 .andExpect(jsonPath("$.data.status").value("PENDING"));
//     }

//     @Test
//     @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
//     void getReturnDetails_WithInvalidId_ShouldReturnNotFound() throws Exception {
//         when(returnRequestService.getRequestById(999L)).thenReturn(Optional.empty());
//         when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(new User()));

//         mockMvc.perform(get("/api/v1/customer/returns/999"))
//                 .andExpect(status().isNotFound())
//                 .andExpect(jsonPath("$.success").value(false));
//     }

//     @Test
//     @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
//     void initiateReturn_WithValidRequest_ShouldCreateReturn() throws Exception {
//         User testUser = new User(1L, "Test Customer", "customer@test.com", "password", User.Role.CUSTOMER);
//         ReturnRequest returnRequest = new ReturnRequest();
//         returnRequest.setId(1L);
//         returnRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
//         returnRequest.setUser(testUser);

//         when(returnRequestService.initiateReturn(any(ReturnRequest.class))).thenReturn(returnRequest);
//         when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(testUser));

//         mockMvc.perform(post("/api/v1/customer/returns")
//                 .with(csrf())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(returnRequest)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.success").value(true))
//                 .andExpect(jsonPath("$.data.id").value(1L));
//     }
// }