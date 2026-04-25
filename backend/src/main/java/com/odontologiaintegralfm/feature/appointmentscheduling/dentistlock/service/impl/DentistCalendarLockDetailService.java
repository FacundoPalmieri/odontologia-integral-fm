package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.impl;


import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLockDetail;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.repository.IDentistCalendarLockDetailRepository;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.IDentistCalendarLockDetailService;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.List;

@Service
public class DentistCalendarLockDetailService implements IDentistCalendarLockDetailService {

    @Autowired
    private IDentistCalendarLockDetailRepository dentistCalendarLockDetailRepository;

    /**
     * Crea todos los detalles asociados a un Dentist Calendar Lock
     *
     * @param dentistCalendarLockDetails : Cabecera de los detalles.
     */
    @Override
    public void saveAll(List<DentistCalendarLockDetail> dentistCalendarLockDetails) {
        try{
            dentistCalendarLockDetailRepository.saveAll(dentistCalendarLockDetails);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockDetailService", null, null, "saveAll");
        }
    }

    /**
     * Método que obtiene los detalles de un dentistCalendarLock por su id.
     * @param idDentistCalendarLock: dentistCalendarLock
     */
    @Override
    public List<DentistCalendarLockDetail> getAllByDentistCalendarLock(Long idDentistCalendarLock) {
        try{
            return dentistCalendarLockDetailRepository.findByDentistCalendarLockId(idDentistCalendarLock);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockDetailService", idDentistCalendarLock, null, "getAllByCalendarLockId");
        }
    }
}
