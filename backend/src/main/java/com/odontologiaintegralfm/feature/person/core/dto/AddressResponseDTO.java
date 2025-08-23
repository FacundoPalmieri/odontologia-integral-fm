package com.odontologiaintegralfm.feature.person.core.dto;

/**
 * @author [Facundo Palmieri]
 */
public record AddressResponseDTO(
        Long localityId,
        String locality,
        Long provinceId,
        String province,
        Long countryId,
        String country,
        String street,
        Integer number,
        String floor,
        String apartment
) {
}
