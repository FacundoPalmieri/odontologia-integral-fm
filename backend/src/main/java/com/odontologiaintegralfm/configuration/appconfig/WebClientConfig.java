package com.odontologiaintegralfm.configuration.appconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configura el cliente HTTP para comunicarse con APIs externas.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient argentinaDatosWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.argentinadatos.com/v1/feriados")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
