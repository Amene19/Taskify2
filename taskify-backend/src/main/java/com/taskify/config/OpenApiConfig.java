package com.taskify.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Taskify API",
        version = "1.0.0",
        description = "Task and Appointment Management REST API. " +
            "This API allows authenticated users to manage their tasks and appointments. " +
            "Use the /api/auth/register endpoint to create an account, " +
            "then /api/auth/login to obtain a JWT token for authentication.",
        contact = @Contact(
            name = "Taskify Support",
            email = "support@taskify.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT authentication. Obtain a token from /api/auth/login and enter it here.",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration is done via annotations
}
