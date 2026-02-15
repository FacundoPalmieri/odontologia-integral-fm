package com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.IDentistCalendarLockService;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.model.Holiday;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.service.IHolidayService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DentistHolidayCreateUseCase implements IDentistHolidayCreateUseCase {

    private final IUserService userService;
    private final IDentistService dentistService;
    private final IHolidayService holidayService;
    private final IDentistCalendarLockService dentistCalendarLockService;
    private final IDentistHolidayService dentistHolidayService;

    public DentistHolidayCreateUseCase(
            IUserService userService,
            IDentistService dentistService,
            IHolidayService holidayService,
            IDentistCalendarLockService dentistCalendarLockService,
            IDentistHolidayService dentistHolidayService
    ){
        this.userService = userService;
        this.dentistService = dentistService;
        this.holidayService = holidayService;
        this.dentistCalendarLockService = dentistCalendarLockService;
        this.dentistHolidayService = dentistHolidayService;
    }


    /**
     * Crea una relación entre un dentista y un feriado, asegurando que no exista
     * un bloqueo de calendario para la fecha indicada y que el dentista y el feriado
     * existan en el sistema.
     *
     * @param userId : Id usuario
     * @param dto    : dto request.
     */
    @LogAction(
            value = "dentistHolidayCreateUseCase.logAction.execute",
            args = {"#result.DentistHolidayResponseDTO.idDentist","#result.DentistHolidayResponseDTO.idHoliday","#result.DentistHolidayResponseDTO.date"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    @Override
    public Response<DentistHolidayResponseDTO> execute(Long userId, DentistHolidayRequestCreateDTO dto) {
        //Obtiene el usuario.
        UserSec userSec = userService.getByIdInternal(userId);

        //Obtiene el dentista
        Dentist dentist = dentistService.getById(userSec.getPerson().getId())
                .orElseThrow(()-> new NotFoundException("exception.dentistNotFound.user", null,"exception.dentistNotFound.log",new Object[]{userSec.getPerson().getId(),"DentistHolidayCreateUseCase","execute"}, LogLevel.ERROR));


        //Obtiene el feriado.
        Holiday holiday = holidayService.getByIdInternal(dto.idHoliday());

        //Valida si no existe un bloqueo para esa fecha.
        List<DentistCalendarLock> dentistCalendarLocks = dentistCalendarLockService.getByDate(dentist.getId(), holiday.getDate());
        if(!dentistCalendarLocks.isEmpty()) {
            throw new ConflictException("exception.validateByIdDentistAndDate.dentistHolidayCreate.user", new Object[]{dentistCalendarLocks.get(0).getId()}, "exception.validateByIdDentistAndDate.dentistHolidayCreate.log", new Object[]{dentistCalendarLocks.get(0).getId(),holiday.getId(), "DentistHolidayCreateUseCase", "execute"}, LogLevel.ERROR);
        }

        // Crear relación
        return dentistHolidayService.create(dentist, holiday, dto);
    }
}
