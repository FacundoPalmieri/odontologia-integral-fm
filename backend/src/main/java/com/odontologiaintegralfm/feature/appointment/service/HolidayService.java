package com.odontologiaintegralfm.feature.appointment.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedSystemService;
import com.odontologiaintegralfm.feature.appointment.repository.IHolidayRepository;
import com.odontologiaintegralfm.feature.appointment.enums.HolidayType;
import com.odontologiaintegralfm.feature.appointment.model.Holiday;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.externalapi.ArgentinaDatosClient;
import com.odontologiaintegralfm.infrastructure.externalapi.HolidayApiResponseDTO;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.enums.SystemUserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class HolidayService implements IHolidayService {

    @Autowired
    private ArgentinaDatosClient argentinaDatosClient;

    @Autowired
    private IHolidayRepository holidayRepository;

    @Autowired
    private AuthenticatedSystemService authenticatedSystemService;



    /**
     * Método que consume ArgentinaDatosClient.
     * Mapea la respuesta recibida de la API a la entidad y persiste en BD.
     *
     * @param year año a consultar
     * @return
     */

    @LogAction(
            value = "holidayService.systemLogService.loadHoliday",
            args = {"#result.durationSeconds", "#result.message", "#result.countInit"},
            type = LogType.SCHEDULED,
            level = LogLevel.INFO
    )
    @Override
    public SchedulerResultDTO loadHolidays(int year) {
        AtomicInteger count = new AtomicInteger(0);
        long start;
        long end;
        double durationSeconds;

        //Inicia tarea programada
        start = System.currentTimeMillis();

        List<HolidayApiResponseDTO> dto = argentinaDatosClient.fetch(year);

        List<Holiday> holidays = dto.stream()
                .map(d -> {
                    Holiday holiday = new Holiday();
                    holiday.setDate(LocalDate.parse(d.fecha()));
                    holiday.setType(HolidayType.fromLabel(d.tipo()));
                    holiday.setName(d.nombre());
                    holiday.setYear(Year.now().getValue() + 1);
                    holiday.setCreatedBy(authenticatedSystemService.getAuthenticatedUserSystem());
                    holiday.setCreatedAt(LocalDateTime.now());

                    count.incrementAndGet();

                    return holiday;
                })
                .toList();

        // Persiste los feriados.
        holidayRepository.saveAll(holidays);

        //Limpia contexto de seguridad.
        SecurityContextHolder.clearContext();

        //Finaliza tarea programada
        end = System.currentTimeMillis();

        //Convierte milisegundos a segundos.
        durationSeconds = (end - start) / 1000.0;

        SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                durationSeconds,
                "Feriados cargados exitosamente",
                count.intValue(),
                0);
        return schedulerResultDTO;

    }
}

