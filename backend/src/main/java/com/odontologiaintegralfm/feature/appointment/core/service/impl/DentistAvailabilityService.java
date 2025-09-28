package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.AppointmentConflictMap;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentConflictReason;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistAvailabilityRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.email.service.IEmailService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.message.service.implement.MessageService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class DentistAvailabilityService implements IDentistAvailabilityService {

    @Autowired
    private IDentistService dentistService;

    @Autowired
    private IDentistAvailabilityRepository dentistAvailabilityRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentConflictService appointmentConflictService;

   @Autowired
   private IEmailService emailService;

   @Autowired
   private IUserService userService;


    /**
     * Método para crear/Actualizar la disponibilidad de turnos de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     *
     * @param days
     */

    @LogAction(
            value = "dentistAvailabilityService.SystemLog.update",
            args = {"#id"}
    )
    @Override
    @Transactional
    public Response<DentistAvailabilityResponseDTO> update(Long id, List<WorkingDayDTO> days) {
        try {
            //Valida que exista dentista
            Dentist dentist = dentistService.getById(id)
                    .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{id, "Dentist Availability Service", "update"}, LogLevel.ERROR));


            //Buscar si existe relación:
            List<DentistAvailability> dentistAvailabilityExisting = dentistAvailabilityRepository.findAllByDentistId(id);

            //Si la lista NO está vacía, existe relación previa entre dentista y disponibilidad entonces se limpia en la base.
            if (!dentistAvailabilityExisting.isEmpty()) {
                dentistAvailabilityRepository.deleteAll(dentistAvailabilityExisting);
            }

            //Se persiste la nueva relación.
            List<DentistAvailability> newAvailabilities = days.stream()
                    .map(dto -> new DentistAvailability(
                            dentist,
                            dto.dayName(),
                            dto.startTime(),
                            dto.endTime(),
                            dto.appointmentDuration(),
                            LocalDateTime.now(),
                            authenticatedUserService.getAuthenticatedUser(),
                            true
                    ))
                    .toList();

            List<DentistAvailability> dentistAvailabilitiesSaved = dentistAvailabilityRepository.saveAll(newAvailabilities);

            //Verificar si hay turnos existentes que se vean afectados.
            List<AppointmentConflictResponseDTO> appointments = detectConflicts(id, days);


            // Se arma la respuesta final.
            DentistAvailabilityResponseDTO dentistAvailabilityResponseDTO = new DentistAvailabilityResponseDTO(
                    dentistAvailabilitiesSaved.get(0).getDentist().getId(),
                    dentistAvailabilitiesSaved
                            .stream()
                            .map(da -> new WorkingDayDTO(
                                    da.getKeyName(),
                                    da.getStartTime(),
                                    da.getEndTime(),
                                    da.getAppointmentDuration()
                            ))
                            .toList(),
                    appointments
            );

            String messageUser = messageService.getMessage("dentistAvailabilityService.update.ok", null, LocaleContextHolder.getLocale());

            return new Response<>(true, messageUser, dentistAvailabilityResponseDTO);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityService", id , null, "update");
        }
    }


    /**
     * Método interno de la aplicación para detectar conflictos con turno futuros ante cambios en la jornada laboral de un dentista.
     * 1. Trae turnos futuros y conflictos existentes del dentista.
     * 2. Detecta conflictos nuevos según la jornada (days).
     * 3. Compara los turnos en conflictos nuevos vs los  que ya existían y separa en una nueva lista solo los nuevos que hay que persistir (appointmentConflictsNewDb).
     * 4. Persiste los nuevos conflictos.
     * 5. Marca como resueltos los conflictos viejos que ya no están en conflicto.
     * 6. Mapea los conflictos persistidos a DTOs para responder.
     *
     * @param idDentist : Id Dentista
     * @param days
     * @return : Turno
     */
    private List<AppointmentConflictResponseDTO> detectConflicts(Long idDentist, List<WorkingDayDTO> days) {

        //Se obtienen los turnos futuros para el dentista.
        List<Appointment> appointments = appointmentService.getAppointmentsInternal(idDentist);

        // Se obtiene los turnos conflictivos previos al cambio.
        List<AppointmentConflict> appointmentConflictsExisting = appointmentConflictService.getAllByDentist(idDentist);

        if (appointments.isEmpty() && appointmentConflictsExisting.isEmpty()) {
            return Collections.emptyList();
        }

        // Se evalúan todos los turnos(en conflicto o no) para determinar si alguno está en conflictos por la nueva parametrización.
        List<AppointmentConflict> appointmentConflictsNew = detectNewConflict(appointments, days);


        //Comparamos turnos en conflictos por la nueva parametrización vs Turnos en conflicto ya existente.
        Map<AppointmentConflictMap, List<AppointmentConflict>> conflictResult = compareConflictNewWithDataBase(appointmentConflictsNew, appointmentConflictsExisting);

        //Persistimos en base solo los nuevos conflictos
        appointmentConflictService.create(conflictResult.get(AppointmentConflictMap.CREATE));

        //Persistimos conflictos reabiertos.
        reopenConflicts(conflictResult.get(AppointmentConflictMap.REOPEN));

        //Persistimos como resueltos aquellos turnos en conflictos, que ya no lo están por la nueva parametrización.
        resolveOldConflicts(conflictResult.get(AppointmentConflictMap.RESOLVED));

        //Mapeamos los conflictos persistidos a UN DTO para respuesta.
        List<AppointmentConflictResponseDTO> conflictsResponseDTO = conflictResult.get(AppointmentConflictMap.FINAL).stream()
                .map(acs -> new AppointmentConflictResponseDTO(
                        acs.getAppointment().getId(),
                        acs.getAppointment().getDate(),
                        acs.getAppointment().getPatient().getPerson().getLastName() + "," + acs.getAppointment().getPatient().getPerson().getFirstName(),
                        AppointmentConflictReason.OUT_OF_SCHEDULE.name(),
                        AppointmentConflictReason.OUT_OF_SCHEDULE.getLabel()
                ))
                .toList();


        //Notificación por mail.
        notifyEmail();

        return conflictsResponseDTO;
    }


    /**
     * Método privado para notificar por mail
     */
    private void notifyEmail() {

        String user = authenticatedUserService.getAuthenticatedUser().getUsername();

        List<String> destination = userService.getEmailByRole(List.of(
                Role.SECRETARY.toString(),
                Role.ADMINISTRATOR.toString(),
                Role.DEVELOPER.toString()
        ));

        // Obtener el mensaje completo con la URL de restablecimiento
        String body = messageService.getMessage("dentistAvailabilityService.notifyEmail.body", new Object[] {user}, LocaleContextHolder.getLocale());

        //Asunto del email
        String subject = messageService.getMessage("dentistAvailabilityService.notifyEmail.subject", new Object[] {user}, LocaleContextHolder.getLocale());

        //Envío de email
        emailService.sendEmail(destination, subject, body);
    }


    /**
     * Método privado que detecta nuevo conflictos ante cambios de parametrización en la jornada del dentista.
     * @param appointments : Lista de turnos futuros.
     * @param days         : Nueva jornada de trabajo.
     */
    private List<AppointmentConflict> detectNewConflict(List<Appointment> appointments, List<WorkingDayDTO> days){
        List<AppointmentConflict> appointmentConflictsNew = appointments.stream()
                .filter(a -> days.stream()
                        .noneMatch(d -> {

                            DayName AppointmentDay = DayName.fromDayOfWeek(a.getDate().getDayOfWeek());
                            // Si el día coincide, debe estar dentro del horario
                            if (d.dayName().equals(AppointmentDay)) {
                                return !a.getDate().toLocalTime().isBefore(d.startTime())
                                        && !a.getDate().toLocalTime().isAfter(d.endTime());
                            }

                            // Si el día no coincide, esta jornada no cubre el turno
                            return false;
                        })
                )
                .map(appointment -> new AppointmentConflict(
                        null,
                        appointment,
                        AppointmentConflictReason.OUT_OF_SCHEDULE,
                        false


                ))
                .toList();

        return appointmentConflictsNew;

    }

    /**
     * Método privado que compara turnos en conflictos por la nueva parametrización vs turnos con conflictos ya existentes.
     * -    Si no hay turnos en conflicto (appointmentConflictsNew), entonces todos los conflictos existentes pasan como resueltos (RESOLVED)
     * -    Si hay turnos en conflictos, se evalúa si ya existen previamente.
     *      -   Si existen, se reabren o se mantienen en conflicto (REOPEN)
     *      -   Sino existen, se crean (CREATE)
     *
     * -    Los conflictos CREATE y REOPEN se agregan a una lista FINAL para devolver en el DTO.
     * @param appointmentConflictsNew      : Turnos con conflictos nuevos.
     * @param appointmentConflictsExisting : Turnos con conflictos existentes.
     * @return
     */
    private Map<AppointmentConflictMap, List<AppointmentConflict>> compareConflictNewWithDataBase(List<AppointmentConflict> appointmentConflictsNew, List<AppointmentConflict> appointmentConflictsExisting) {

        List<AppointmentConflict> appointmentConflictCreate = new ArrayList<>();
        List<AppointmentConflict> appointmentConflictUpdateResolved = new ArrayList<>();
        List<AppointmentConflict> appointmentConflictUpdateReopen = new ArrayList<>();
        List<AppointmentConflict> appointmentConflictFinal = new ArrayList<>();


        //Se indexan IDs existentes para búsqueda directa.
        Map<Long, AppointmentConflict> existingById = new HashMap<>();
        for (AppointmentConflict existing : appointmentConflictsExisting) {
            existingById.put(existing.getAppointment().getId(), existing);

        }

        //Si no hay conflictos antes la nueva parametrización, todos los existentes pasan a resueltos.
        if (appointmentConflictsNew.isEmpty()) {
            appointmentConflictUpdateResolved.addAll(appointmentConflictsExisting);
        } else {
            //Si hay conflictos por la nueva parametrización, comparo con los conflictos existentes para saber si es necesario crearlos o actualizarlos.
            for (AppointmentConflict appointmentConflictNew : appointmentConflictsNew) {
                AppointmentConflict match = existingById.get(appointmentConflictNew.getAppointment().getId());
                if (match != null) { // Si el conflicto existe, se reabre
                    appointmentConflictUpdateReopen.add(match);
                    appointmentConflictFinal.add(match);
                } else { // Si el conflicto no existe previamente, se crea.
                    AppointmentConflict ac = new AppointmentConflict(
                            appointmentConflictNew.getId(),
                            appointmentConflictNew.getAppointment(),
                            AppointmentConflictReason.OUT_OF_SCHEDULE,
                            false,
                            LocalDateTime.now(),
                            authenticatedUserService.getAuthenticatedUser(),
                            true
                    );
                    appointmentConflictCreate.add(ac);
                    appointmentConflictFinal.add(ac);
                }
            }
        }

        Map<AppointmentConflictMap, List<AppointmentConflict>> result = new HashMap<>();
        result.put(AppointmentConflictMap.CREATE, appointmentConflictCreate);
        result.put(AppointmentConflictMap.REOPEN, appointmentConflictUpdateReopen);
        result.put(AppointmentConflictMap.RESOLVED, appointmentConflictUpdateResolved);
        result.put(AppointmentConflictMap.FINAL, appointmentConflictFinal);

        return result;
    }

    /**
     * Método privado para reabrir conflictos resueltos por una nueva inconsistencia en la prametrización del odontólogo.
     *
     * @param appointmentConflictUpdate
     */
    private void reopenConflicts(List<AppointmentConflict> appointmentConflictUpdate) {

        appointmentConflictUpdate
                .forEach(appointmentConflict -> {
                    appointmentConflict.setResolved(false);
                    appointmentConflict.setAppointmentConflictReason(AppointmentConflictReason.OUT_OF_SCHEDULE);
                    appointmentConflict.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                    appointmentConflict.setUpdatedAt(LocalDateTime.now());
                    appointmentConflictService.update(appointmentConflict);
                });
    }


    /**
     * Método privado que compara y actualiza turnos en conflictos previos que ya no se encuentren en conflicto con la nueva parametrización.
     */
    private void resolveOldConflicts(List<AppointmentConflict> resolved) {
        resolved.forEach(conflictExisting -> {
                    conflictExisting.setResolved(true);
                    conflictExisting.setAppointmentConflictReason(null);
                    conflictExisting.setUpdatedAt(LocalDateTime.now());
                    conflictExisting.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                }
        );


        appointmentConflictService.update(resolved);

    }





    /**
     * Método para obtener la disponibilidad de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     *
     * @param id : Id del dentista
     */
    @Override
    public Response<DentistAvailabilityResponseDTO> get(Long id) {
        try {


            //Valida que exista dentista
            Dentist dentist = dentistService.getById(id)
                    .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{id, "Dentist Availability Service", "update"}, LogLevel.ERROR));

            //Buscar si existe relación:
            List<DentistAvailability> dentistAvailability= dentistAvailabilityRepository.findAllByDentistId(id);

            if(dentistAvailability.isEmpty()) {
                throw new NotFoundException("dentistAvailabilityService.notFound.user",null,"dentistAvailabilityService.notFound.log",new Object[]{id, "Dentist Availability Service", "get"}, LogLevel.WARN);
            }

            //Mapeo a un DTO
            DentistAvailabilityResponseDTO dentistAvailabilityResponseDTO = new DentistAvailabilityResponseDTO(
                    dentistAvailability.get(0).getDentist().getId(),
                    dentistAvailability
                            .stream()
                            .map(da -> new WorkingDayDTO(
                                    da.getKeyName(),
                                    da.getStartTime(),
                                    da.getEndTime(),
                                    da.getAppointmentDuration()
                            ))
                            .toList(),
                    null
            );

            return new Response<>(true, null, dentistAvailabilityResponseDTO);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityService", id, null, "get");
        }
    }



}
