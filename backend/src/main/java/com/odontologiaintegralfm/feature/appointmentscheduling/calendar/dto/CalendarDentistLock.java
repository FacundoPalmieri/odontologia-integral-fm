package com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto;

import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;

/**
 * DTO que se devuelve en las vistas de calendario.
 * Se muestra algunos datos del bloqueo del dentista para identificar
 * el motivo en caso de que el día tenga el estado LOCKED
 *
 * No se utiliza {@link DentistCalendarLockResponseDTO} ya que tiene muchos atributos innecesarios para la vista
 */
public record CalendarDentistLock(
        Long dentistCalendarLockId,
        String lockType
) {
}
