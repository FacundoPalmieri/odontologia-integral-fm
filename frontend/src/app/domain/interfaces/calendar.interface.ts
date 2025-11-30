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
