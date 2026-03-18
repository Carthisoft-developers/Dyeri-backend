// com/dyeri/core/infrastructure/config/OpenApiConfig.java
package com.dyeri.core.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${keycloak.auth-server-url:http://localhost:8180}")
    private String keycloakUrl;

    @Value("${keycloak.realm:dyeri}")
    private String realm;

    @Bean
    public OpenAPI dyeriOpenAPI() {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        return new OpenAPI()
                .info(new Info().title("Dyeri API").description("Home-cooked meal ordering – Tunisia").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("keycloak"))
                .components(new Components().addSecuritySchemes("keycloak",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .clientCredentials(new OAuthFlow().tokenUrl(tokenUrl))
                                        .password(new OAuthFlow().tokenUrl(tokenUrl)))));
    }
}
