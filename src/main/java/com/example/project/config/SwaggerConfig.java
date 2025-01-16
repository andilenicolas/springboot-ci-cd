package com.example.project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPIWithJWT() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .info(new Info().title("Project API").version("1.0")
                .description("API documentation with JWT authentication")
                .contact(new Contact().name("Andile Sithole").email("your.email@example.com"))
            );
    }

    @Bean
    public GroupedOpenApi Apis() {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/api/**")
            .build();
    }
    
    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi
        		.builder()
                .group("Actuator")
                .pathsToMatch("/actuator/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                		new Info()
                        .title("Actuator Endpoints")
                        .description("Monitoring and health check endpoints for the application.")
                        .version("1.0.0")
                 ))
                .build();
    }
}
