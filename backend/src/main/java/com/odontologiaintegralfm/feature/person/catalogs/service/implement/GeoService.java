package com.odontologiaintegralfm.feature.person.catalogs.service.implement;

import com.odontologiaintegralfm.feature.person.catalogs.dto.CountryResponseDTO;
import com.odontologiaintegralfm.feature.person.catalogs.dto.LocalityResponseDTO;
import com.odontologiaintegralfm.feature.person.catalogs.dto.ProvinceResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.person.catalogs.model.Country;
import com.odontologiaintegralfm.feature.person.catalogs.model.Locality;
import com.odontologiaintegralfm.feature.person.catalogs.model.Province;
import com.odontologiaintegralfm.feature.person.catalogs.repository.ICountryRepository;
import com.odontologiaintegralfm.feature.person.catalogs.repository.ILocalityRepository;
import com.odontologiaintegralfm.feature.person.catalogs.repository.IProvinceRepository;
import com.odontologiaintegralfm.feature.person.catalogs.service.interfaces.IGeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;


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
        try {
            //Obtiene los países.
            List<Country> countries = countryRepository.findAllByEnabledTrue();

            //Convierte los países a un objeto DTO.
            List<CountryResponseDTO> countriesDTO = countries.stream()
                    .map(country -> new CountryResponseDTO(country.getId(), country.getName()))
                    .toList();

            // Retorna respuesta.
            return new Response<>(true, "", countriesDTO);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "GeoService", 0L, "", "getAllCountries");
        }
    }


    /**
     * Valída el ID de país recibido.
     * Obtiene todas las provincias habilitadas correspondientes al ID del País que recibe como parámetro.
     *
     * @param countryId id del país.
     * @return Una respuesta que contiene una lista de objetos {@link ProvinceResponseDTO} representando las provincias.
     */
    @Override
    public Response<List<ProvinceResponseDTO>> getProvincesByIdCountry(Long countryId) {
        try {
            //Valída que el País exista o esté habilitado.
            validateCountry(countryId);

            // Recupera provincias relacionadas que estén habilitadas.
            List<Province> provinces = provinceRepository.findAllByCountryIdAndEnabledTrue(countryId);

            //Convierte las provincias en un objeto DTO.
            List<ProvinceResponseDTO> provincesDTO = provinces.stream()
                    .map(province -> new ProvinceResponseDTO(province.getId(), province.getName())
                    ).toList();

            // Retorna respuesta.
            return new Response<>(true, "", provincesDTO);
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
        try {

            //Valída que la Provincia exista o esté habilitado.
            validateProvince(provinceId);

            //Obtener todas las localidades por ID de provincia
            List<Locality> localities = localityRepository.findAllByProvinceIdAndEnabledTrue(provinceId);

            //Convierte las localidades a un objeto DTO
            List<LocalityResponseDTO> localitiesDTO = localities.stream()
                    .map(locality -> new LocalityResponseDTO(locality.getId(), locality.getName()))
                    .toList();

            //Retorna respuesta
            return new Response<>(true, "", localitiesDTO);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "GeoService", null, null, "getLocalitiesByIdProvinces");
        }
    }

    /**
     * Método para obtener una "Localidad" de acuerdo al ID recibido como parámetro.
     *
     * @param localityId del tipo de Localidad.
     * @return {@link Locality} con el tipo de Género encontrado.
     */
    @Override
    public Locality getLocalityById(Long localityId) {
        try{
            return localityRepository.findByIdAndEnabledTrue(localityId).orElseThrow(()-> new NotFoundException("exception.localityNotFound.user", null, "exception.localityNotFound.log",new Object[]{ localityId,"GeoService","getLocalityById"}, LogLevel.ERROR));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "GeoService", localityId, null, "getLocalityById");
        }
    }


    /**
     * Valída que el País recibido por parámetro exista y este habilitado.
     * @param countryId

     */
    private void validateCountry(Long countryId) {
        countryRepository.findByIdAndEnabledTrue(countryId)
                .orElseThrow(() -> new NotFoundException("geoService.validateCountry.notFound.user", null,"geoService.validateCountry.notFound.log", new Object[]{ countryId,"GeoService","validateCountry"},LogLevel.ERROR));
    }

    /**
     * Valída que la Provincia recida por parámetros exista y esté habilitada.
     * @param provinceId
     * @throws NotFoundException en caso de no encontrar la provincia.
     */
    private void validateProvince(Long provinceId) {
        provinceRepository.findByIdAndEnabledTrue(provinceId)
                .orElseThrow(() -> new NotFoundException("geoService.validateProvince.notFound.user", null,"geoService.validateProvince.notFound.log",new Object[]{ provinceId,"GeoService","validateProvince"},LogLevel.ERROR));
    }

}



