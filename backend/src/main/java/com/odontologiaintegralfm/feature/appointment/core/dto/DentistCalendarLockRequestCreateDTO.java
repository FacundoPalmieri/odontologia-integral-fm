package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
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
 * - Bloqueo puntual:
 *      days vacío, recurrence NONE, startDate == endDate
 *
 * - Bloqueo diario:
 *      days vacío, recurrence DAILY o null (en cuyo caso se fuerza DAILY),
 *      startDate != endDate
 *
 * - Bloqueo recurrente:
 *      days no vacío
 *      recurrence != DAILY
 *      recurrence null → se interpreta como NONE
 *
 * La validación final se realiza en servicio antes de persistir.
 */
@Getter
@Setter
public class DentistCalendarLockRequestCreateDTO {

 @NotNull(message = "dentistCalendarLockRequestCreateDTO.lockType.empty")
 private Long idLockType;

 private List<DayName> days;

 private CalendarLockRecurrenceName recurrence;

 @NotNull(message = "dentistCalendarLockRequestCreateDTO.startDate.empty")
 private LocalDate startDate;

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
