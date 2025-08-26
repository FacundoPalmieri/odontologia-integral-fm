package com.odontologiaintegralfm.infrastructure.externalapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 *  Cliente que consume la API
 */

@Service
public class ArgentinaDatosClient {

    @Autowired
    private  WebClient argentinaDatosWebClient;

    public List<HolidayApiResponseDTO> fetch(int year){
        return argentinaDatosWebClient.get()
                .uri("/{year}/", year) //Construye URL.
                .retrieve() //Ejecuta solicitud.
                .bodyToFlux(HolidayApiResponseDTO.class) // Convierte la respuesta JSON al DTO
                .collectList()
                .block(); // Bloquea el hilo hasta recibir la respuesta completa
    }
}
