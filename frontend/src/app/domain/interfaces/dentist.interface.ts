import { DayEnum } from "../../utils/enums/day.enum";

export interface DentistInterface {
  licenseNumber: string;
  dentistSpecialty: DentistSpecialtyInterface;
}

export interface DentistSpecialtyInterface {
  id: number;
  name: string;
}

export interface AppointmentConflictInterface {
  idAppointment: number;
}

export interface TimeInterface {
  hour: number;
  minute: number;
  second: number;
  nano: 0;
}

export interface DentistDayAvailabilityInterface {
  dayName: DayEnum;
  startTime: TimeInterface;
  endTime: TimeInterface;
  appointmentDuration: number;
}

export interface TimeInterface {
  hour: number;
  minute: number;
  second: number;
  nano: 0;
}
