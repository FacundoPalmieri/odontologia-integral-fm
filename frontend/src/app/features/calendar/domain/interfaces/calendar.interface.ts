import { CalendarMonthDayStatusEnum } from "../../utils/enums/calendar-month-day-status.enum";
import { SlotStatusEnum } from "../../utils/enums/slot-status.enum";
import { CalendarLockDayInterface } from "./calendar-lock.interface";

export interface CalendarMonthInterface {
  year: number;
  month: number;
  days: CalendarMonthDayInterface[];
}

export interface CalendarMonthDayInterface {
  calendarDayStatus: CalendarDayStatusInterface;
  day: string;
  dentistHolidayId: number;
  dentistId: number;
  holiday: CalendarHolidayInterface;
  slots: CalendarSlotInterface[];
}

export interface CalendarWeekInterface {
  weekStart: Date;
  weekEnd: Date;
  days: CalendarDayInterface[];
}

export interface CalendarDayInterface {
  dentistId: number;
  dentistHolidayId: number;
  day: Date;
  calendarDayStatus: CalendarDayStatusInterface;
  slots: CalendarSlotInterface[];
  holiday: CalendarHolidayInterface;
}

export interface CalendarHolidayInterface {
  id: number;
  key: CalendarMonthDayStatusEnum;
  color: string;
  description: string;
  label: string;
  type: string;
}

export interface CalendarDayStatusInterface {
  key: CalendarMonthDayStatusEnum;
  description: string;
  color: string;
}

export interface CalendarSlotInterface {
  startTime: string;
  endTime: string;
  status: SlotStatusEnum;
  color: string;
  appointment: CalendarAppointmentInterface;
  calendarLock: CalendarLockDayInterface;
}

export interface CalendarAppointmentInterface {
  id: number;
  dentistName: string;
  idPatient: number;
  patientName: string;
  appointmentDateTime: Date;
  status: SlotStatusEnum;
}
