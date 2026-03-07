import { AppointmentConflictInterface } from "../../../appointments/data/interfaces/appointment.inteface";
import { DayEnum } from "../../../../shared/utils/enums/day.enum";
import { RecurrenceEnum } from "../../../../shared/utils/enums/recurrence.enum";

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

export interface TimeInterface {
  hour: number;
  minute: number;
}
