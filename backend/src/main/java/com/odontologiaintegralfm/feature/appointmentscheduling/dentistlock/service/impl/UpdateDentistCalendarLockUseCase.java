package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.AppointmentConflictService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.IUpdateDentistCalendarLockUseCase;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;


@Service
public class UpdateDentistCalendarLockUseCase implements IUpdateDentistCalendarLockUseCase {



    private final DentistCalendarLockService dentistCalendarLockService;
    private final MessageSource messageSource;
    private final AppointmentConflictService appointmentConflictService;
    private final AuthenticatedUserService authenticatedUserService;

    public UpdateDentistCalendarLockUseCase(DentistCalendarLockService dentistCalendarLockService,
                                            MessageSource messageSource,
                                            AppointmentConflictService appointmentConflictService,
                                            AuthenticatedUserService authenticatedUserService
    ) {
        this.dentistCalendarLockService = dentistCalendarLockService;
        this.messageSource = messageSource;
        this.appointmentConflictService = appointmentConflictService;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Actualiza un bloqueo existente en el calendario de un dentista.
     * <p>
     * El método realiza las siguientes operaciones:
     * <ol>
     *     <li>Recupera el bloqueo a actualizar a partir del ID proporcionado.</li>
     *     <li>Verifica que el bloqueo aún esté vigente; si ya finalizó, lanza una excepción de conflicto.</li>
     *     <li>Resuelve los turnos en conflicto posteriores a la finalización anticipada del bloqueo, si corresponde.</li>
     *     <li>Actualiza la información del bloqueo (por ejemplo, la observación de actualización y la fecha de fin).</li>
     *     <li>Mapea los datos actualizados a un {@link DentistCalendarLockResponseDTO} para la respuesta.</li>
     * </ol>
     *
     * @param dentistCalendarLockRequestUpdateDTO DTO que contiene los datos de actualización del bloqueo, incluyendo ID del bloqueo y observación de actualización.
     * @return {@link Response} que contiene el DTO con la información actualizada del bloqueo.
     * @throws BadRequestException Si el bloqueo con el ID proporcionado no se encuentra.
     * @throws ConflictException Si el bloqueo ya finalizó y no se puede actualizar.
     */

    @Override
    @LogAction(
            value = "dentistCalendarLockUpdateUseCase.logAction.execute",
            args  = {"#dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock","#result.data.endDate","#result.data.ObservationUpdate"},
            type  = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<DentistCalendarLockResponseDTO> execute(DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO) {


        //Recupera, valída vigencia y actualiza fecha de finalización y persiste.
        DentistCalendarLock dentistCalendarLockSaved = dentistCalendarLockService.update(dentistCalendarLockRequestUpdateDTO);


        //Resuelve turnos en conflicto posterior a la finalización anticipada del bloqueo.
        appointmentConflictService.resolvedAppointmentConflictByFinishLock(dentistCalendarLockSaved,authenticatedUserService.getAuthenticatedUser());


        return new Response<>(
                true,
                messageSource.getMessage("dentistCalendarLockUpdateUseCase.execute.ok.user",null, LocaleContextHolder.getLocale()),
                DentistCalendarLockResponseDTO.build(dentistCalendarLockSaved, null)
        );

    }
}
