package com.microloan.backend.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI api() {
    return new OpenAPI()
      .info(new Info().title("Microloan · First 50% API").version("1.0"))
      .components(
        new Components().addSecuritySchemes(
          "bearerAuth",
          new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
        )
      );
  }

  @Bean
  GroupedOpenApi auth() {
    return group("Authentication", "/api/auth/**");
  }

  @Bean
  GroupedOpenApi profile() {
    return group("Borrower Profile", "/api/borrower/profile");
  }

  @Bean
  GroupedOpenApi borrowerKyc() {
    return group("Borrower KYC", "/api/borrower/kyc/**");
  }

  @Bean
  GroupedOpenApi borrowerLoans() {
    return group("Borrower Loans", "/api/borrower/loans/**");
  }

  @Bean
  GroupedOpenApi officerKyc() {
    return group("Officer KYC", "/api/officer/kyc/**");
  }

  @Bean
  GroupedOpenApi officerLoans() {
    return group("Officer Loans", "/api/officer/loans/**");
  }

  private GroupedOpenApi group(String name, String path) {
    return GroupedOpenApi.builder().group(name).pathsToMatch(path).build();
  }
}
