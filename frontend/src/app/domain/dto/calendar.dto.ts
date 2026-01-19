import { DayEnum, RecurrenceEnum } from "../../utils/enums/day.enum";
import { LockTypeModeEnum } from "../../utils/enums/calendar/lock-type-mode.enum";

export interface CalendarLockCreateDtoInterface {
  idLockType: number;
  mode: LockTypeModeEnum;
  days: DayEnum[];
  recurrence: RecurrenceEnum;
  startDate: Date;
  endDate: Date;
  startTime: string;
  endTime: string;
  observation: string;
}

export interface CalendarLockUpdateDtoInterface {
  idDentistCalendarLock: number;
  observationUpdate: string;
}
