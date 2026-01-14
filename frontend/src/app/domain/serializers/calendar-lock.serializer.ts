import { CalendarLockCreateDtoInterface } from "../dto/calendar.dto";
import { CalendarLockInterface } from "../interfaces/calendar.interface";

export class CalendarLockSerializer {
  static toCreateDto(
    calendarLock: CalendarLockInterface
  ): CalendarLockCreateDtoInterface {
    const calendarLockDto: CalendarLockCreateDtoInterface = {
      idLockType: calendarLock.calendarLockType.id,
      mode: calendarLock.mode,
      days: calendarLock.days,
      recurrence: calendarLock.recurrence,
      startDate: calendarLock.startDate,
      endDate: calendarLock.endDate,
      startTime: calendarLock.startTime,
      endTime: calendarLock.endTime,
      observation: calendarLock.observation,
    };

    return calendarLockDto;
  }
}
