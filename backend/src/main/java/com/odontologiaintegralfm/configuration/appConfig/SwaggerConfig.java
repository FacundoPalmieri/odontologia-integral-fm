package com.odontologiaintegralfm.configuration.appConfig;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.HttpHeaders;

@OpenAPIDefinition(
        info = @Info(
                title = "Odontología Integral FM - API REST",
                description = "API RESTful diseñada para gestionar los servicios y operaciones de la aplicación web de Odontología Integral FM.",
                version = "1.0.0"
                /*
                license = @License(
                        name = "Prueba",
                        url = "prueba",
                        identifier = "123.143"
                )
                 */

        ),
        servers = {
                @Server(
                        description = "DEV SERVER",
                        url = "http://localhost:8080"
                )
                /*,
                @Server(
                        description = "Producción",
                        url = "Producción"
                )
                */

        },
        security = @SecurityRequirement(
                name  = "Security Token"
        )
)
@SecurityScheme(
        name = "Security Token",
        description = "Acceso con Token",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {



}

