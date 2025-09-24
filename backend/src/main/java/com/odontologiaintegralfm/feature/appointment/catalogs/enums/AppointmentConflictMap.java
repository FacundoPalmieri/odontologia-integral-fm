package com.odontologiaintegralfm.feature.appointment.catalogs.enums;


/**
 * Enum que se utiliza como key para el map del método #compareConflictNewWithDataBase
 * de {@link com.odontologiaintegralfm.feature.appointment.core.service.impl.AppointmentService}
 */
public enum AppointmentConflictMap {
    CREATE,
    RESOLVED,
    REOPEN,
    FINAL
}
