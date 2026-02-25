import { DayEnum } from "../../../../shared/utils/enums/day.enum";
import { RecurrenceEnum } from "../../../../shared/utils/enums/recurrence.enum";
import { LockTypeModeEnum } from "../../utils/enums/lock-type-mode.enum";

export interface CalendarLockCreateDto {
  idLockType: number;
  mode: LockTypeModeEnum;
  days: DayEnum[];
  recurrence: RecurrenceEnum;
  startDate: Date;
  endDate: Date;
  fullDay: boolean;
  startTime: string;
  endTime: string;
  observation: string;
}

export interface CalendarLockUpdateDto {
  idDentistCalendarLock: number;
  observationUpdate: string;
}
