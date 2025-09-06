package com.odontologiaintegralfm.feature.person.catalogs.dto;

import lombok.Data;

/**
 * DTO que representa la respuesta con un País.
 */
@Data
public class CountryResponseDTO {

    private Long id;

    private String name;


    public CountryResponseDTO (Long id, String name){
        this.id =id;
        this.name = name;
    }

}

