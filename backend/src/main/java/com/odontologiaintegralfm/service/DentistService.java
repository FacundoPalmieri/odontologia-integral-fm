package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.*;
import com.odontologiaintegralfm.repository.IDentistRepository;
import com.odontologiaintegralfm.service.interfaces.*;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DentistService implements IDentistService {
    @Autowired
    private IPersonService personService;

    @Autowired
    private IDentistRepository dentistRepository;

    @Autowired
    private DentistSpecialtyService dentistSpecialtyService;

    @Autowired
    @Lazy
    private AuthenticatedUserService authenticatedUserService;



    /**
     * Crea un nuevo odontólogo en el sistema junto con su domicilio, contactos, historia clínica
     * y devuelve un objeto  Dentist
     *
     * <p>Este método:
     * <ol>
     *     <li>Valida que no exista un odontólogo con el mismo DNI.</li>
     *     <li>Crea un nuevo objeto {@link Dentist} y lo persiste.</li>
     * </ol>
     * @param person Objeto ya persistido en la base con los datos referidos a la Persona.
     * @param dentistCreateRequestDTO DTO con los datos necesarios para específicamente un odontólogo.
     * @return {@link Dentist} que contiene el dentista persistido en la base de datos.
     * @throws DataBaseException si ocurre un error durante el acceso a la base de datos.
     */
    @Override
    @Transactional
    public Dentist create(Person person, DentistCreateRequestDTO dentistCreateRequestDTO) {
        try{
            //Valída que no exista el n.° de licencia.
            validateDentist(dentistCreateRequestDTO.licenseNumber());

            Dentist dentist = new Dentist();
            dentist.setPerson(person);
            dentist.setLicenseNumber(dentistCreateRequestDTO.licenseNumber());
            dentist.setDentistSpecialty(dentistSpecialtyService.getById(dentistCreateRequestDTO.dentistSpecialtyId()));
            dentist.setCreatedAt(LocalDateTime.now());
            dentist.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            dentist.setEnabled(true);
            return dentistRepository.save(dentist);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService", person.getId(),person.getLastName() + "," + person.getFirstName(), "create");
        }
    }

    /**
     * Método para actualizar un Dentista.
     *
     * @param dentist                 datos del objeto Dentista recuperado desde la BD.
     * @param dentistUpdateRequestDTO datos nuevos recibidos en el DTO.
     * @return Dentista
     */
    @Override
    @Transactional
    public Dentist update(Dentist dentist, DentistUpdateRequestDTO dentistUpdateRequestDTO) {
        try{
            //Valída que no exista el n.° de licencia.
            validateDentist(dentistUpdateRequestDTO.licenseNumber());

            dentist.setLicenseNumber(dentistUpdateRequestDTO.licenseNumber());
            dentist.setDentistSpecialty(dentistSpecialtyService.getById(dentistUpdateRequestDTO.dentistSpecialtyId()));

            dentist.setUpdatedAt(LocalDateTime.now());
            dentist.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
            return dentistRepository.save(dentist);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService", dentist.getId(),null, "update");
        }
    }

    /**
     * Método para obtener un listado de pacientes habilitados en el sistema.
     *
     * @return Una respuesta que contiene una lista de objetos {@link DentistResponseDTO }
     */
    @Override
    public Response<Set<DentistResponseDTO>> getAll() {
        try{
            Set<Dentist> dentists = dentistRepository.findAllByEnabledTrue();
            Set <DentistResponseDTO> dentistDTO = dentists.stream()
                    .map(dentist -> new DentistResponseDTO(personService.convertToDTO(dentist.getPerson()), dentist.getLicenseNumber(), dentist.getDentistSpecialty().getName())
                            )
                    .collect(Collectors.toSet());

            return new Response<>(true, null, dentistDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService",null,null, "getAll");
        }
    }

    /**
     * Método para obtener un dentista por su Id
     *
     * @param id del Dentista.
     * @return objeto Dentist
     */
    @Override
    public Dentist getById(Long id) {
        try{
            return dentistRepository.findById(id)
                    .orElseThrow(() ->  new NotFoundException("exception.dentistNotFound.user",null,"exception.dentistNotFound.log",new Object[]{id,"DentistService","getById"}, LogLevel.ERROR));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService",null,null, "getById");
        }
    }

    private void validateDentist(String licenseNumber) {
        try{
            Optional<Dentist> dentist  = dentistRepository.findByLicenseNumberAndEnabledTrue(licenseNumber);
            if(dentist.isPresent()){
                throw new ConflictException("exception.licenseNumber.user", null,"exception.licenseNumber.log", new Object[]{licenseNumber,"DentistService", "validateDentist"}, LogLevel.ERROR);
            }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistService", null, licenseNumber, "validateDentist");
        }
    }



    /**
     * Método para transformar un {@link Dentist} a un {@link DentistResponseDTO}
     * @param dentist objeto completo
     * @return DentistResponseDTO
     */
    public DentistResponseDTO convertToDTO(Dentist dentist) {
        return new DentistResponseDTO(
                personService.convertToDTO(dentist.getPerson()),
                dentist.getLicenseNumber(),
                dentist.getDentistSpecialty().getName()
        );
    }
}
