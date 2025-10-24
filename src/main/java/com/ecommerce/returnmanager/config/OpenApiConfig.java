package com.ecommerce.returnmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Return & Refund Management System API")
                .description("""
                    A comprehensive REST API for managing product returns and refunds in an e-commerce system.
                    
                    ## Features:
                    - Customers can initiate return requests
                    - Admins can approve/reject return requests  
                    - Automatic refund calculation
                    - 30-day return window validation
                    - Role-based access control
                    
                    ## Authentication:
                    - Form-based login for web interface
                    - Basic Auth for API endpoints
                    - Role-based authorization (CUSTOMER/ADMIN)
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("API Support")
                    .email("support@ecommerce.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://springdoc.org")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .name("bearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}