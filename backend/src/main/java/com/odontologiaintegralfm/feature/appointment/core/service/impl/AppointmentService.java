package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentConflictReason;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AppointmentService implements IAppointmentService {

    @Autowired
    private IAppointmentRepository appointmentRepository;


    /**
     * Método interno de la aplicación para obtener conflictos con turno futuros ante cambios en la jornada laboral de un dentista.
     * Se usa para validaciones.
     *
     * @param idDentist : Id Dentista
     * @param days
     * @return : Turno
     */
    @Override
    public List<AppointmentConflictResponseDTO> getConflict(Long idDentist, List<WorkingDayDTO> days) {

        //Se obtienen los turnos futuros para el dentista.
       List<Appointment> appointments = appointmentRepository.findFutureAppointmentsByDentist(idDentist,LocalDateTime.now());

       //Se evalúan conflictos.
        if(appointments.isEmpty()){
            return Collections.emptyList();
        }

        //Filtra por cada turno, si alguno coincide el día o el horario dentro de la nueva jornada del dentista.
        List<AppointmentConflictResponseDTO> conflicts = appointments.stream()
                .filter(a -> days.stream()
                        .anyMatch(d -> d.dayName().equals(DayName.fromDayOfWeek(a.getDate().getDayOfWeek()))
                                && (a.getDate().toLocalTime().isBefore(d.startTime())
                                || a.getDate().toLocalTime().isAfter(d.endTime()))
                        )
                )
                .map(a -> new AppointmentConflictResponseDTO(
                        a.getId(),
                        a.getDate(),
                        a.getPatient().getPerson().getLastName() + ", " + a.getPatient().getPerson().getFirstName(),
                        AppointmentConflictReason.OUT_OF_SCHEDULE.toString(),
                        AppointmentConflictReason.OUT_OF_SCHEDULE.getLabel()

                ))
                .toList();

        return conflicts;
    }
}
