// package com.ecommerce.returnmanager.config;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
// @AutoConfigureMockMvc
// public class SecurityConfigTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Test
//     public void publicEndpoints_ShouldBeAccessible() throws Exception {
//         mockMvc.perform(get("/"))
//                .andExpect(status().isOk());

//         mockMvc.perform(get("/login"))
//                .andExpect(status().isOk());
//     }

//     @Test
//     @WithMockUser(roles = "CUSTOMER")
//     public void customerApiEndpoints_WithCustomerRole_ShouldBeAccessible() throws Exception {
//         mockMvc.perform(get("/api/v1/customer/returns/1"))
//                .andExpect(status().isOk());
//     }

//     @Test
//     @WithMockUser(roles = "ADMIN")
//     public void adminEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
//         mockMvc.perform(get("/admin/dashboard"))
//                .andExpect(status().isOk());
//     }
// }