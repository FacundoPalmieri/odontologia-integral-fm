package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.CountryResponseDTO;
import com.odontologiaintegralfm.dto.LocalityResponseDTO;
import com.odontologiaintegralfm.dto.ProvinceResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import java.util.List;

/**
 * Interfaz que proporciona métodos para obtener datos de Geografía
 */
public interface IGeoService {

    /**
     * Obtiene todos los países
     * @return Una respuesta que contiene una lista de objetos {@link CountryResponseDTO} representando los países.
     */
    Response<List<CountryResponseDTO>> getAllCountries();


    /**
     * Obtiene todas las provincias correspondientes al ID del País que recibe como parámetro.
     * @param countryId id del país.
     * @return Una respuesta que contiene una lista de objetos {@link ProvinceResponseDTO} representando las provincias.
     */
    Response<List<ProvinceResponseDTO>> getProvincesByIdCountry(Long countryId);

    /**
     *     * Obtiene todas las localidades correspondientes al ID de la provincia que recibe como parámetro.
     * @param provinceId id de la provincia
     * @return Una respuesta que contiene una lista de objetos {@link LocalityResponseDTO} representando las localidades.
     */
    Response<List<LocalityResponseDTO>> getLocalitiesByIdProvinces(Long provinceId);

}
