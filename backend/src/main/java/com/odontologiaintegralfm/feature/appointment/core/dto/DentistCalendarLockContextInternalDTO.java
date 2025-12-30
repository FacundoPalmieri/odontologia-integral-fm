package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;

/**
 * DTO interno del servicio DentistCalendarLock.
 * Se utiliza para preparar el contexto de validación de dentist y su jornada actual,
 * antes de Crear una nueva, o hacer un preview de los posibles conflictos ante la intención de actualizar la misma.
 */
public record DentistCalendarLockContextInternalDTO(
        DentistCalendarLockRequestCreateDTO dto,
        DentistCalendarLock dentistCalendarLock
) {
    public static DentistCalendarLockContextInternalDTO build(DentistCalendarLockRequestCreateDTO dto,   DentistCalendarLock dentistCalendarLock) {
        return new DentistCalendarLockContextInternalDTO(dto, dentistCalendarLock);
    }
}
