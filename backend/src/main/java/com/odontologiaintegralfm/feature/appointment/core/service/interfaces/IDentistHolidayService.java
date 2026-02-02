package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistHoliday;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
public interface IDentistHolidayService {

    /**
     * Método que crea relación entre dentista y feriado.
     */
    Response<DentistHolidayResponseDTO> create(Long idUser, DentistHolidayRequestCreateDTO dentistHolidayRequestCreateDTO);

    /**
     * Método para la actualización de la relación de un dentista con feriados.
     */
    Response<DentistHolidayResponseDTO> update(DentistHolidayRequestUpdateDTO dentistHolidayRequestUpdateDTO);

    /**
     * Obtiene las relaciones entre dentista y feriados.
     * @param idDentist : id dentista
     * @param year : año a consultar
     * @return : Lista
     */
    List<DentistHoliday> getAll(Long idDentist, int year);

    /**
     * Obtiene la relación entre dentista y feriado.
     * @param idDentist: id dentista
     * @param idHoliday: id feriado.
     */
    Optional<DentistHoliday> getByDentistIdAndHolidayId(Long idDentist, Long idHoliday);


    /**
     * Valída si existe relación entre feriado y dentista.
     * Si existe, no realiza acción.
     * Si no existe, arroja exceptión.
     * @param idDentist : Id dentista
     * @param date : Fecha
     */
    void validateDentistIdAndDate(Long idDentist, LocalDate date);


    /**
     * Verifica si existe relación entre un Holiday y dentista. Caso afirmativo, deshabilita la relación.
     */
    void VerifyAndDisabled (Long idHoliday ,Long idDentist);
}
