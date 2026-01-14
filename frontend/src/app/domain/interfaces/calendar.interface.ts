import {
  CalendarMonthDayStatusEnum,
  SlotStatusEnum,
} from "../../utils/enums/appointment/appointment-status.enum";
import { LockTypeModeEnum } from "../../utils/enums/calendar/lock-type-mode.enum";
import { DayEnum, RecurrenceEnum } from "../../utils/enums/day.enum";

export interface CalendarLockTypeInterface {
  id: number;
  name: string;
  enable: boolean;
  modes: (CalendarLockTypeModeInterface | string)[];
}

export interface CalendarLockTypeModeInterface {
  name: string;
  label: string;
  description: string;
}

export interface CalendarLockInterface {
  calendarLockType: CalendarLockTypeInterface;
  mode: LockTypeModeEnum;
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
  date: string;
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
  calendarDayStatus: CalendarDayStatusInterface;
  slots: SlotInterface[];
  holiday: HolidayInterface;
}

export interface HolidayInterface {
  key: CalendarMonthDayStatusEnum;
  color: string;
  description: string;
  label: string;
  type: string;
}

export interface CalendarDayStatusInterface {
  key: SlotStatusEnum;
  description: string;
  color: string;
}

export interface SlotInterface {
  startTime: string;
  endTime: string;
  status: SlotStatusEnum;
  color: string;
  appointment: AppointmentInterface;
  calendarLock: CalendarLockDayInterface;
}

export interface CalendarLockDayInterface {
  id: number;
  idDentist: number;
  appointmentConflict: any;
  startDate: string;
  startTime: string;
  endDate: string;
  endTime: string;
  lockType: string;
  observation: string;
  observationUpdate: any;
  recurrence: string;
}

export interface AppointmentInterface {
  id: number;
  dentistName: string;
  idPatient: number;
  patientName: string;
  appointmentDateTime: Date;
  status: SlotStatusEnum;
}
