import { DayEnum, RecurrenceEnum } from "../../utils/enums/day.enum";

export interface CalendarLockCreateDtoInterface {
  idLockType: number;
  days: DayEnum[];
  recurrence: RecurrenceEnum;
  startDate: Date;
  endDate: Date;
  startTime: string;
  endTime: string;
  observation: string;
}
