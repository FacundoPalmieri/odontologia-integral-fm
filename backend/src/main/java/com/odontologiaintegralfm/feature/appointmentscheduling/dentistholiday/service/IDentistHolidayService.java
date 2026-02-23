package com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.model.Holiday;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.model.DentistHoliday;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
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
    Response<DentistHolidayResponseDTO> create(Dentist dentist, Holiday holiday, DentistHolidayRequestCreateDTO dto );

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
