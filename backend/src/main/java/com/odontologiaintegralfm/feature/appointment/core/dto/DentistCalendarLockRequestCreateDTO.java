package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockMode;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;



/**
 * DTO para la creación de un bloqueo de agenda.
 *
 * Reglas principales según combinación de campos:
 *
 * - Bloqueo día puntual:
 *      days vacío, recurrence NONE, startDate == endDate
 *
 * - Bloqueo días en una semana puntual:
 *      days vacío, recurrence WEEKLY, startDate(primer dia de la semana) != endDate(último dia de la semana)
 *
 * - Bloqueo diario:
 *      days vacío, recurrence DAILY o null (en cuyo caso se fuerza DAILY),
 *      startDate != endDate
 *
 * - Bloqueo recurrente:
 *      days no vacío
 *      recurrence:
 *      != DAILY
 *      != NONE
 *      != NULL
 * La validación final se realiza en servicio antes de persistir.
 */
@Getter
@Setter
public class DentistCalendarLockRequestCreateDTO {

 @NotNull(message = "dentistCalendarLockRequestCreateDTO.lockType.empty")
 private Long idLockType;

 @NotNull(message = "dentistCalendarLockRequestCreateDTO.mode.empty")
 private CalendarLockMode mode;

 private List<DayName> days;

 private CalendarLockRecurrenceName recurrence;


 /**
  * Fecha inicial del rango del bloqueo.
  *
  * Su interpretación depende del escenario:
  *
  * - Bloqueo puntual: es la única fecha bloqueada.
  * - Bloqueo diario: inicio del período bloqueado completo.
  * - Bloqueo recurrente (WEEKLY, etc.): semana ancla desde donde se
  *   evaluarán los dayName del detalle.
  */
 @NotNull(message = "dentistCalendarLockRequestCreateDTO.startDate.empty")
 @Future(message = "generic.date.futureOrPresent")
 private LocalDate startDate;



 /**
  * Fecha final del rango del bloqueo.
  *
  * Debe ser >= startDate.
  *
  * - Bloqueo puntual: igual a startDate.
  * - Bloqueo diario: fin del período bloqueado.
  * - Bloqueo recurrente: semana ancla final para evaluar el patrón de días.
  */
 @NotNull(message = "dentistCalendarLockRequestCreateDTO.endDate.empty")
 private LocalDate endDate;



 private LocalTime startTime;

 private LocalTime endTime;

 private String observation;






 /**
  * Solo uso interno: id propio de esta entidad que es la que puede generar conflictos
  */
 @Null
 @JsonProperty(access = JsonProperty.Access.READ_ONLY)
 private Long idOriginConflict;


 /**
  * Solo uso interno: motivo/origen del conflicto
  */
 @Null
 @JsonProperty(access = JsonProperty.Access.READ_ONLY)
 private OriginConflict originConflict;


}
