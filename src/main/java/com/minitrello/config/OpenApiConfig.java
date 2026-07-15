package com.minitrello.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  private static final String BEARER_SCHEME_NAME = "bearerAuth";

  @Bean
  public OpenAPI miniTrelloOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Mini Trello API")
                .description(
                    "Backend API for a small Trello-like task management app "
                        + "(Board -> TaskList -> Task -> Comment), built with Spring Boot, "
                        + "Spring Security (JWT), and Spring Data JPA.")
                .version("v1"))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME_NAME,
                    new SecurityScheme()
                        .name(BEARER_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
