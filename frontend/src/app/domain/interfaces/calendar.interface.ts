import {
  CalendarMonthDayStatusEnum,
  SlotStatusEnum,
} from "../../utils/enums/appointment/appointment-status.enum";
import { DayEnum, RecurrenceEnum } from "../../utils/enums/day.enum";

export interface CalendarLockTypeInterface {
  id: number;
  name: string;
  enable: boolean;
}

export interface CalendarLockInterface {
  calendarLockType: CalendarLockTypeInterface;
  days: DayEnum[];
  recurrence: RecurrenceEnum;
  startDate: Date;
  endDate: Date;
  startTime: string;
  endTime: string;
  observation: string;
}

export interface CalendarMonthInterface {
  year: number;
  month: number;
  days: CalendarMonthDayInterface[];
}

export interface CalendarMonthDayInterface {
  date: string; // Formato: "YYYY-MM-DD"
  status: CalendarMonthDayStatusEnum;
  description: string;
  color: string;
}

export interface CalendarWeekInterface {
  weekStart: Date;
  weekEnd: Date;
  days: CalendarDayInterface[];
}

export interface CalendarDayInterface {
  dentistId: number;
  day: Date;
  calendarDayStatus: SlotStatusEnum;
  slots: SlotInterface[];
}

export interface SlotInterface {
  startTime: string;
  endTime: string;
  status: SlotStatusEnum;
  color: string;
  appointment: AppointmentInterface;
}

export interface AppointmentInterface {
  id: number;
  dentistName: string;
  patientName: string;
  appointmentDateTime: Date;
  status: SlotStatusEnum;
}
