package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums;


import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.impl.AppointmentService;

/**
 * Enum que se utiliza como key para el map del método #compareConflictNewWithDataBase
 * de {@link AppointmentService}
 */
public enum AppointmentConflictMap {
    CREATE,
    RESOLVED,
    REOPEN,
    FINAL
}
