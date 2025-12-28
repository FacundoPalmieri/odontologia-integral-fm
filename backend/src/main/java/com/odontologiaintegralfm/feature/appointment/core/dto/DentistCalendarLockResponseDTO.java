package com.odontologiaintegralfm.feature.appointment.core.dto;



import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DentistCalendarLockResponseDTO(
        Long id,
        Long idDentist,
        String lockType,
        String recurrence,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        String observation,
        String observationUpdate,
        List<AppointmentConflictResponseDTO> appointmentConflict
) {

    public static DentistCalendarLockResponseDTO build(DentistCalendarLock dentistCalendarLock){
        return new DentistCalendarLockResponseDTO(
                dentistCalendarLock.getId(),
                dentistCalendarLock.getDentist().getId(),
                dentistCalendarLock.getType().getName(),
                dentistCalendarLock.getRecurrence().getLabel(),
                dentistCalendarLock.getStartDate(),
                dentistCalendarLock.getEndDate(),
                dentistCalendarLock.getStartTime(),
                dentistCalendarLock.getEndTime(),
                dentistCalendarLock.getObservation(),
                dentistCalendarLock.getObservationUpdate(),
                null
        );
    }


    public static DentistCalendarLockResponseDTO build(DentistCalendarLock dentistCalendarLock, List<AppointmentConflictResponseDTO> appointmentConflictResponseDTO){
        return new DentistCalendarLockResponseDTO(
                dentistCalendarLock.getId(),
                dentistCalendarLock.getDentist().getId(),
                dentistCalendarLock.getType().getName(),
                dentistCalendarLock.getRecurrence().getLabel(),
                dentistCalendarLock.getStartDate(),
                dentistCalendarLock.getEndDate(),
                dentistCalendarLock.getStartTime(),
                dentistCalendarLock.getEndTime(),
                dentistCalendarLock.getObservation(),
                dentistCalendarLock.getObservationUpdate(),
                appointmentConflictResponseDTO
        );
    }
}
