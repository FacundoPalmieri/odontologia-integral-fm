import { DayEnum, RecurrenceEnum } from "../../utils/enums/day.enum";
import { AppointmentConflictInterface } from "./appointment.inteface";

export interface DentistInterface {
  licenseNumber: string;
  dentistSpecialty: DentistSpecialtyInterface;
}

export interface DentistSpecialtyInterface {
  id: number;
  name: string;
}

export interface TimeInterface {
  hour: number;
  minute: number;
}

export interface DentistDayAvailabilityInterface {
  dayName?: DayEnum | null;
  recurrence?: RecurrenceEnum | null;
  specificDate: Date | string | null;
  startTime: TimeInterface;
  endTime: TimeInterface;
  appointmentDuration: number;
  breakStartTime: TimeInterface;
  breakEndTime: TimeInterface;
}

export interface DentistAvailabilityResponseInterface {
  idDentist: number;
  days: DentistDayAvailabilityInterface[];
}

export interface DentistAvailabilitySaveResponseInterface {
  idDentist: number;
  days: DentistDayAvailabilityInterface[];
  appointmentConflict: AppointmentConflictInterface[];
}
