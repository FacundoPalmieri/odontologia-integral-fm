package com.odontologiaintegralfm.feature.appointment.catalogs.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record CalendarLockTypeCreateRequestDTO(
        @NotNull(message = "calendarLockTypeCreateRequestDTO.name.empty")
        String name,

        boolean absenceTotal,

        boolean allowTimeRange,

        boolean allowDays
) {

        @AssertTrue(message = "calendarLockTypeCreateRequestDTO.assert")
        public boolean isValidAbsenceConfiguration() {
                if (absenceTotal) {
                        return !allowTimeRange && !allowDays;
                }
                return true;
        }


}
