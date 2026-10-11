package com.jobtrack.config;

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

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI jobTrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JobTrack REST API")
                        .version("1.0.0")
                        .description("### Production-Grade Job Application Management System API\n\n"
                                + "JobTrack provides full end-to-end management of job applications, interview rounds, "
                                + "application notes, document/resume attachments, automated reminders, and pipeline analytics.\n\n"
                                + "#### Authentication Instructions:\n"
                                + "1. Call `POST /api/v1/auth/login` with your credentials (`demo@jobtrack.com` / `password123`) or register a new user.\n"
                                + "2. Copy the `token` string from the JSON response.\n"
                                + "3. Click the **Authorize 🔓** button on the top right, enter your token in the Value field, and click **Authorize**.\n"
                                + "4. All authenticated endpoints are now unlocked for testing directly in Swagger UI!")
                        .contact(new Contact()
                                .name("JobTrack Engineering Team")
                                .email("engineering@jobtrack.com")
                                .url("https://github.com/"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT Bearer token.")));
    }
}
