package com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.IAppointmentConflictService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.IAppointmentService;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service.ICreateDentistAvailabilityUseCase;
import com.odontologiaintegralfm.feature.appointmentscheduling.shared.ConflictManagerContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.DentistAvailabilityContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.email.service.IEmailService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class CreateDentistAvailabilityUseCase implements ICreateDentistAvailabilityUseCase {

    private final IAppointmentConflictService appointmentConflictService;
    private final IAppointmentService appointmentService;
    private final IUserService userService;
    private final MessageSource messageSource;
    private final IEmailService emailService;
    private final AuthenticatedUserService authenticatedUserService;
    private final DentistAvailabilityService dentistAvailabilityService;
    private final IDentistService dentistService;


    public CreateDentistAvailabilityUseCase(
            IAppointmentConflictService appointmentConflictService,
            IAppointmentService appointmentService,
            IUserService userService,
            MessageSource messageSource,
            IEmailService emailService,
            AuthenticatedUserService authenticatedUserService,
            DentistAvailabilityService dentistAvailabilityService,
            IDentistService dentistService){
        this.appointmentConflictService = appointmentConflictService;
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.messageSource = messageSource;
        this.emailService = emailService;
        this.authenticatedUserService = authenticatedUserService;
        this.dentistAvailabilityService = dentistAvailabilityService;
        this.dentistService = dentistService;
    }




    /**
     * Crea o actualiza la disponibilidad de turnos de un dentista.
     *
     * <p>Este método realiza las siguientes acciones:</p>
     * <ol>
     *     <li>Valida que el dentista exista.</li>
     *     <li>Deshabilita las disponibilidades actuales activas para evitar inconsistencias con la nueva jornada.</li>
     *     <li>Crea y persiste las nuevas disponibilidades según la lista de {@link WorkingDayDTO} recibida.</li>
     *     <li>Asocia cada disponibilidad creada con un posible origen de conflicto ({@link OriginConflict AVAILABILITY}).</li>
     *     <li>Verifica si existen turnos existentes que se vean afectados por la nueva disponibilidad, generando {@link AppointmentConflictResponseDTO}.</li>
     *     <li>Construye y retorna un {@link DentistAvailabilityResponseDTO} con las nuevas disponibilidades y los conflictos detectados.</li>
     * </ol>
     *
     * <p>Notas importantes:</p>
     * <ul>
     *     <li>La jornada siempre entra en vigencia a partir del día siguiente de la actualización para evitar superposición con jornadas activas y turnos ya generados.</li>
     *     <li>Si existen disponibilidades previas, se deshabilitan antes de persistir las nuevas.</li>
     *     <li>Las disponibilidades contienen información de auditoría: {@code createdAt} y {@code createdBy}.</li>
     * </ul>
     *
     * @param id   ID del dentista cuya disponibilidad se actualizará. No puede ser {@code null}.
     * @param days Lista de {@link WorkingDayDTO} que define la nueva jornada laboral.
     * @return {@link Response} con un {@link DentistAvailabilityResponseDTO} que contiene:
     * <ul>
     *     <li>El ID del dentista.</li>
     *     <li>La lista de nuevas disponibilidades con horarios, recurrencias y duración de turno.</li>
     *     <li>Los conflictos detectados respecto a turnos existentes ({@link AppointmentConflictResponseDTO}).</li>
     * </ul>
     * @throws ConflictException si el dentista no existe en la base de datos.
     * @throws DataBaseException si ocurre un error de acceso a la base de datos o fallo en la transacción.
     */
    @LogAction(
            value = "dentistAvailabilityCreateUseCase.SystemLog.create",
            args = {"#id"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Override
    @Transactional
    public Response<DentistAvailabilityResponseDTO> execute(Long id, List<WorkingDayDTO> days) {
        try {

            DentistAvailabilityContextInternalDTO dentistAvailabilityExisting = prepareContext(id);


            //Si la lista no está vacía existe relación previa entre dentista y disponibilidad. Se deshabilitan las mismas.
            if (!dentistAvailabilityExisting.dentistAvailabilities().isEmpty()) {
                dentistAvailabilityService.disabledAvailability(dentistAvailabilityExisting.dentistAvailabilities(),authenticatedUserService, LocalDateTime.now());
            }


            //Se llama al dominio para mapear y persistir
            List<DentistAvailability> dentistAvailabilitiesSaved = dentistAvailabilityService.create(days, dentistAvailabilityExisting, authenticatedUserService.getAuthenticatedUser());


            //Se completa el mapeo del DTO con los datos para posible origen de conflicto (Id entidad persistida en cada jornada DTO)
            completeDto(days,dentistAvailabilitiesSaved);


            //Verificar si hay turnos existentes que se vean afectados.
            List<AppointmentConflictResponseDTO> appointmentsConflict = verifyConflictsByDentistAvailability(id, days);


            return new Response<>(
                    true,
                    (appointmentsConflict.isEmpty())
                            ? messageSource.getMessage("dentistAvailabilityCreateUseCase.execute.ok.user",null, LocaleContextHolder.getLocale())
                            : messageSource.getMessage("dentistAvailabilityCreateUseCase.execute.okWithConflict.user", null, LocaleContextHolder.getLocale()),
                    DentistAvailabilityResponseDTO.build(dentistAvailabilitiesSaved,appointmentsConflict));
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityCreateUseCase", id, null, "execute");
        }
    }








    /**
     * Método para simular una nueva jornada laboral de un dentista.
     * @param id : id dentista
     * @param days : Lista de jornadas.
     */
    @Override
    public Response<DentistAvailabilityResponseDTO> executePreview(Long id, List<WorkingDayDTO> days) {


        DentistAvailabilityContextInternalDTO dentistAvailabilityExisting = prepareContext(id);


        //Mapeo a entidad
        List<DentistAvailability> newAvailabilities = dentistAvailabilityService.entityFromDto(days, dentistAvailabilityExisting,authenticatedUserService.getAuthenticatedUser());


        //Si no hay disponibilidades previas, no hay conflictos. Se retorna.
        if(dentistAvailabilityExisting.dentistAvailabilities().isEmpty()) {
            return new Response<>(
                    true,
                    messageSource.getMessage("dentistAvailabilityCreateUseCase.executePreview.ok.user", null, LocaleContextHolder.getLocale()),
                    DentistAvailabilityResponseDTO.build(newAvailabilities,List.of()));
        }


        //Se completa el mapeo del DTO con los datos para posible origen de conflicto.
        completeDto(days,newAvailabilities);

        //Verificar si hay turnos existentes que se vean afectados.
        List<AppointmentConflictResponseDTO> appointmentsConflict = PreviewVerifyConflictsByDentistAvailability(id, days);

        return new Response<>(
                true,
                (appointmentsConflict.isEmpty())
                        ? messageSource.getMessage("dentistAvailabilityCreateUseCase.executePreview.ok.user",null, LocaleContextHolder.getLocale())
                        : messageSource.getMessage("dentistAvailabilityCreateUseCase.executePreview.okWithConflict.user", null, LocaleContextHolder.getLocale()),
                DentistAvailabilityResponseDTO.build(newAvailabilities,appointmentsConflict));

    }


    /**
     * Método para verificar conflictos ante consulta de posible conflictos, antes un preview de cambios en la jornada laboral del dentista.
     *
     * @param idDentist : id Dentista.
     * @param days      : Lista con DTOs qie tienen la nueva jornada laboral.
     * @return : Lista de AppointmentConflictResponseDTO
     */
    public List<AppointmentConflictResponseDTO> PreviewVerifyConflictsByDentistAvailability(Long idDentist, List<WorkingDayDTO> days) {

        //Obtiene turnos y turnos conflictivos.
        ConflictManagerContextInternalDTO conflictManagerContextInternalDTO =  prepareContextByAvailability(idDentist);


        //Si no hay ninguno, no hay más nada para evaluar.
        if (conflictManagerContextInternalDTO.appointments().isEmpty() && conflictManagerContextInternalDTO.appointmentConflicts().isEmpty()) {
            return Collections.emptyList();
        }


        // Se reevalúan todos los turnos(en conflicto o no) para determinar si alguno está en conflicto por la nueva parametrización.
        List<AppointmentConflict> appointmentConflictsNew = dentistAvailabilityService.evaluateAppointmentDentistAvailability(conflictManagerContextInternalDTO.appointments(),days);


        //Mapeamos los conflictos persistidos a UN DTO para respuesta.
        return appointmentConflictsNew.stream()
                .map(AppointmentConflictResponseDTO::build)
                .toList();

    }




    /**
     * Método privado que prepara el contexto con la validación del dentista y sus disponibiliades actuales,
     * para la creación de una nueva jornada, o preview de conflicto ante la intención de actualizar la misma
     * @param dentistId : id dentista.
     */
    private DentistAvailabilityContextInternalDTO prepareContext(Long dentistId){
        //Valida que exista dentista
        Dentist dentist = dentistService.getById(dentistId)
                .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{dentistId, "dentistAvailabilityCreateUseCase", "prepareContext"}, LogLevel.ERROR));


        //Buscar si existe relación:
        List<DentistAvailability> dentistAvailabilityExisting = dentistAvailabilityService.findById(dentistId);


        //Valída times de los breaks.
        dentistAvailabilityService.validateBreak(dentistAvailabilityExisting);

        return DentistAvailabilityContextInternalDTO.build(dentist, dentistAvailabilityExisting);
    }




    /**
     * Método interno del servicio.
     * Permite agregar datos al DTO de jornadas de trabajo, agregando el ID de la disponibilidad ya persistida y un motivo de conflicto.
     *
     * @param days                  : DTO con la jornada
     * @param dentistAvailabilities : Jornada persistida que cuenta con el ID.
     */
    private void completeDto(List<WorkingDayDTO> days, List<DentistAvailability> dentistAvailabilities) {

        for (int x = 0; x < dentistAvailabilities.size(); x++) {
            WorkingDayDTO day = days.get(x);
            DentistAvailability dentistAvailability = dentistAvailabilities.get(x);

            day.setEffectiveDate(dentistAvailability.getEffectiveDate());
            day.setIdOriginConflict(dentistAvailability.getId());
            day.setOriginConflict(OriginConflict.DENTIST_AVAILABILITIES);
        }
    }









    /**
     * Verifica y gestiona los conflictos de turnos futuros de un dentista ante cambios en su jornada laboral.
     * <p>
     * Este método detecta inconsistencias entre los turnos programados y la nueva disponibilidad configurada
     * para el dentista. Realiza una reevaluación completa de los turnos futuros, comparando los conflictos
     * recién detectados con los ya existentes en la base de datos, actualizando su estado según corresponda.
     * </p>
     *
     * <p>Flujo general:</p>
     * <ol>
     *   <li>Obtiene los turnos futuros del dentista.</li>
     *   <li>Obtiene los conflictos existentes cuyo origen sea la disponibilidad del dentista.</li>
     *   <li>Evalúa los turnos nuevamente en base a la nueva jornada laboral para detectar nuevos conflictos.</li>
     *   <li>Compara los conflictos nuevos con los existentes, determinando cuáles crear, reabrir o resolver.</li>
     *   <li>Persiste los cambios detectados en la base de datos.</li>
     *   <li>Mapea los conflictos finales a DTOs para la respuesta y envía una notificación por correo electrónico.</li>
     * </ol>
     *
     * @param idDentist Identificador único del dentista cuya disponibilidad fue modificada.
     * @param days Lista de objetos {@link WorkingDayDTO} que representan la nueva jornada laboral configurada.
     * @return Lista de {@link AppointmentConflictResponseDTO} con los conflictos actualizados luego de la reevaluación.
     */
    private List<AppointmentConflictResponseDTO> verifyConflictsByDentistAvailability(Long idDentist, List<WorkingDayDTO> days) {

        //Obtiene turnos y turnos conflictivos.
        ConflictManagerContextInternalDTO conflictManagerContextInternalDTO =  prepareContextByAvailability(idDentist);


        //Si no hay ninguno, no hay más nada para evaluar.
        if (conflictManagerContextInternalDTO.appointments().isEmpty() && conflictManagerContextInternalDTO.appointmentConflicts().isEmpty()) {
            return Collections.emptyList();
        }


        //Se limpian los conflictos previos con origen "Disponibilidad Laboral".
        appointmentConflictService.updateResolvedConflicts(conflictManagerContextInternalDTO.appointmentConflicts(), authenticatedUserService.getAuthenticatedUser());

        // Se reevalúan todos los turnos(en conflicto o no) para determinar si alguno está en conflicto por la nueva parametrización.
        List<AppointmentConflict> appointmentConflictsNew = dentistAvailabilityService.evaluateAppointmentDentistAvailability(conflictManagerContextInternalDTO.appointments(),days);

        //Persistimos en base solo los nuevos conflictos
        List<AppointmentConflict> conflictSaved = appointmentConflictService.create(appointmentConflictsNew);


        //Mapeamos los conflictos persistidos a UN DTO para respuesta.
        List<AppointmentConflictResponseDTO> conflictsResponseDTO = conflictSaved.stream()
                .map(AppointmentConflictResponseDTO::build)
                .toList();


        //Notificación por mail.
        emailService.sendEmail(
                userService.getEmailByRole(List.of(Role.SECRETARY.toString(), Role.ADMINISTRATOR.toString())),
                messageSource.getMessage("dentistAvailabilityCreateUseCase.notifyEmail.subject", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale()),
                messageSource.getMessage("dentistAvailabilityCreateUseCase.notifyEmail.body", new Object[]{authenticatedUserService.getAuthenticatedUser().getUsername()}, LocaleContextHolder.getLocale())
        );

        return conflictsResponseDTO;
    }






    /**
     * Método privado del servicio.
     * Permite obtener turnos y turnos en conflicto.
     * Este método puede ser llamado por:
     * El método para detecta y persistir turnos en conflictos ante nueva jornada laboral.
     * El método que preview de Jornada laboral, para consultar los posibles conflictos antes de actualizar.
     * @param idDentist: id dentista
     */
    private ConflictManagerContextInternalDTO prepareContextByAvailability(Long idDentist){

        //Se obtienen los turnos futuros para el dentista.
        List<Appointment> appointments = appointmentService.getFutureAppointmentsReservedByDentist(idDentist);

        // Se obtiene los turnos conflictivos previos al cambio, y que el origen del conflicto fue la jornada laboral del dentista.
        List<AppointmentConflict> appointmentConflictsExisting = appointmentConflictService.getAllByDentistIdAndAvailabilityConflict(idDentist, OriginConflict.DENTIST_AVAILABILITIES);

        return ConflictManagerContextInternalDTO.build(appointments, appointmentConflictsExisting);

    }


}
