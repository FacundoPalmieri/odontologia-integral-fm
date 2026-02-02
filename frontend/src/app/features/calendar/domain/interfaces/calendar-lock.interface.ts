import { LockTypeModeEnum } from "../../utils/enums/lock-type-mode.enum";
import { DayEnum } from "../../../../shared/utils/enums/day.enum";
import { RecurrenceEnum } from "../../../../shared/utils/enums/recurrence.enum";

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
