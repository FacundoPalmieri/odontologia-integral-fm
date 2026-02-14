package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLockDetail;

import java.util.List;

public interface IDentistCalendarLockDetailService {

    /**
     * Crea todos los detalles asociados a un Dentist Calendar Lock
     *
     * @param dentistCalendarLockDetails : Cabecera de los detalles.
     */
    void saveAll(List<DentistCalendarLockDetail> dentistCalendarLockDetails);

    /**
     * Método que obtiene los detalles de un dentistCalendarLock por su id.
     * @param id: dentistCalendarLock
     */
    List<DentistCalendarLockDetail> getAllByDentistCalendarLock(Long id);
}
