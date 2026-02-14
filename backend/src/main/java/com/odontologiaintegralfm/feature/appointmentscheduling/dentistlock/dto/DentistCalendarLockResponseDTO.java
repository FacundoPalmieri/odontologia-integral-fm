package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto;



import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
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
        boolean isFullDay,
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
                dentistCalendarLock.isFullDay(),
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
                dentistCalendarLock.isFullDay(),
                dentistCalendarLock.getObservation(),
                dentistCalendarLock.getObservationUpdate(),
                appointmentConflictResponseDTO
        );
    }
}
