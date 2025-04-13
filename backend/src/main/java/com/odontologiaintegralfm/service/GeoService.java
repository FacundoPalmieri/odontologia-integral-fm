package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.CountryResponseDTO;
import com.odontologiaintegralfm.dto.LocalityResponseDTO;
import com.odontologiaintegralfm.dto.ProvinceResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Country;
import com.odontologiaintegralfm.model.Locality;
import com.odontologiaintegralfm.model.Province;
import com.odontologiaintegralfm.repository.ICountryRepository;
import com.odontologiaintegralfm.repository.ILocalityRepository;
import com.odontologiaintegralfm.repository.IProvinceRepository;
import com.odontologiaintegralfm.service.interfaces.IGeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class GeoService implements IGeoService {
    @Autowired
    private ICountryRepository countryRepository;

    @Autowired
    private IProvinceRepository provinceRepository;

    @Autowired
    private ILocalityRepository localityRepository;


    /**
     * Obtiene todos los países habilitados.
     *
     * @return Una respuesta que contiene una lista de objetos {@link CountryResponseDTO} representando los países.
     */
    @Override
    public Response<List<CountryResponseDTO>> getAllCountries() {
        try{
            //Obtiene los países.
            List<Country> countries = countryRepository.findAllByEnabledTrue();

            //Convierte los países a un objeto DTO.
            List<CountryResponseDTO> countriesDTO = countries.stream()
                    .map(country -> new CountryResponseDTO(country.getId(), country.getName()))
                    .toList();

            // Retorna respuesta.
            return new Response<>(true,"",countriesDTO);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "GeoService", 0L, "", "getAllCountries");
        }
    }


    /**
     * Obtiene todas las provincias habilitadas correspondientes al ID del País que recibe como parámetro.
     *
     * @param countryId id del país.
     * @return Una respuesta que contiene una lista de objetos {@link ProvinceResponseDTO} representando las provincias.
     */
    @Override
    public Response<List<ProvinceResponseDTO>> getProvincesByIdCountry(Long countryId) {
        try{

            // Obtiene las provincias de acuerdo al ID del pais.
            List<Province> provinces = provinceRepository.findAllByCountryIdAndEnabledTrue(countryId);

            //Convierte las provincias en un objeto DTO.
            List<ProvinceResponseDTO> provincesDTO = provinces.stream()
                    .map(province -> new ProvinceResponseDTO(province.getId(), province.getName())
                    ).toList();

            // Retorna respuesta.
            return new Response<>(true,"",provincesDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "GeoService", 0L, "", "getProvincesByIdCountry");
        }
    }

    /**
     * * Obtiene todas las localidades habilitadas correspondientes al ID de la provincia que recibe como parámetro.
     *
     * @param provinceId id de la provincia
     * @return Una respuesta que contiene una lista de objetos {@link LocalityResponseDTO} representando las localidades.
     */
    @Override
    public Response<List<LocalityResponseDTO>> getLocalitiesByIdProvinces(Long provinceId) {
        try{

            //Obtener todas las localidades por ID de provincia
            List<Locality> localities = localityRepository.findAllByProvinceIdAndEnabledTrue(provinceId);

            //Convierte las localidades a un objeto DTO
            List<LocalityResponseDTO> localitiesDTO = localities.stream()
                    .map(locality -> new LocalityResponseDTO(locality.getId(), locality.getName()))
                    .toList();

            //Retorna respuesta
            return new Response<>(true,"",localitiesDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "GeoService", 0L, "", "getLocalitiesByIdProvinces");
        }
    }


}


