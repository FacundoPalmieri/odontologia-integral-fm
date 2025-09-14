import { DayEnum } from "../../utils/enums/day.enum";

export interface DentistInterface {
  licenseNumber: string;
  dentistSpecialty: DentistSpecialtyInterface;
}

export interface DentistSpecialtyInterface {
  id: number;
  name: string;
}

export interface DentistAvailabilityInterface {
  idDentist: number;
  days: DentistAvailabilityDayInterface[];
  appointConflict?: AppointmentConflictInterface[];
}

export interface DentistAvailabilityDayInterface {
  dayName: DayEnum;
  startTime: TimeInterface;
  endTime: TimeInterface;
  appointmentDuration: number;
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
