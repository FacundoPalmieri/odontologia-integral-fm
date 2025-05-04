package com.odontologiaintegralfm.dto;

import lombok.Data;

/**
 * @author [Facundo Palmieri]
 */
@Data
public class LocalityResponseDTO {

    private Long id;

    private String name;

    public LocalityResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
