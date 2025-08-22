package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.DentistSpecialtyResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.DentistSpecialty;
import com.odontologiaintegralfm.repository.IDentistSpecialtyRepository;
import com.odontologiaintegralfm.service.interfaces.IDentistSpecialtyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.List;

@Service
public class DentistSpecialtyService implements IDentistSpecialtyService {
    @Autowired
    private IDentistSpecialtyRepository dentistSpecialtyRepository;

    /**
     * Método para obtener una Especialidad "habilitada" de acuerdo al ID recibido como parámetro.
     *
     * @param id del tipo de Especialidad.
     * @return {@link DentistSpecialtyService} con el tipo de Especialidad.
     */
    @Override
    public DentistSpecialty getById(Long id) {
        try{
            return dentistSpecialtyRepository.findByIdAndEnabledTrue(id).
                    orElseThrow(()-> new NotFoundException("exception.dentistSpecialtyNotFound.user", null, "exception.dentistSpecialtyNotFound.log", new Object[]{ id,"DentistSpecialtyService","getById"}, LogLevel.ERROR));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistSpecialtyService", id, null, "getById");
        }
    }

    /**
     * Método para obtener todas las especialidades de odontólogos "habilitadas"
     *
     * @return Response<List < DentistSpecialty>>
     */
    @Override
    public Response<List<DentistSpecialtyResponseDTO>> getAll() {
        try{
            List<DentistSpecialty> dentistSpecialties = dentistSpecialtyRepository.findAllByEnabledTrue();

            List<DentistSpecialtyResponseDTO> dentistSpecialtyDTOS = dentistSpecialties
                    .stream()
                    .map(dentistSpecialty -> new DentistSpecialtyResponseDTO(dentistSpecialty.getId(), dentistSpecialty.getName()))
                    .toList();

            return new Response<>(true, null, dentistSpecialtyDTOS);

        }catch(DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistSpecialtyService", null, e.getMessage(), "getAll");
        }
    }
}
