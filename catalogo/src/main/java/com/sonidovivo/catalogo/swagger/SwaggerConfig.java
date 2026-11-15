package com.sonidovivo.catalogo.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Ingrese un token JWT válido para probar operaciones protegidas."
)
public class SwaggerConfig {

    @Bean
    public OpenAPI sonidoVivoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Catálogo de Sonido Vivo")
                        .description("API REST para consultar y administrar categorías, productos e inventario.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sonido Vivo")
                        )
                );
    }
}
