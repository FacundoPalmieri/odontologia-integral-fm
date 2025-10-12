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
  appointmentId: number;
  appointmentDateTime: Date;
  patientName: string;
  reasonKey: string;
  reasonLabel: string;
}

export interface TimeInterface {
  hour: number;
  minute: number;
}

export interface DentistAvailabilityInterface {
  idDentist: number;
  days: DentistDayAvailabilityInterface[];
}

export interface DentistDayAvailabilityInterface {
  dayName: DayEnum;
  startTime: TimeInterface;
  endTime: TimeInterface;
  appointmentDuration: number;
}

export interface DentistAvailabilitySaveResponseInterface
  extends DentistAvailabilityInterface {
  appointmentConflict: AppointmentConflictInterface[];
}
