package com.datacorp.app.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc OpenAPI configuration reflecting the API contract in openapi.yaml.
 * source_legacy: N/A (greenfield API layer)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sifapOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("SIFAP API")
                        .version("v1")
                        .description("Sistema de Fiscalização e Administração de Pagamentos — REST API modernizada")
                        .contact(new Contact()
                                .name("Datacorp")
                                .email("sifap@datacorp.com"))
                        .license(new License().name("Internal")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
