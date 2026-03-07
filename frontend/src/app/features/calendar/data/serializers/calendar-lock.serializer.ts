import {
  CalendarLockCreateDto,
  CalendarLockUpdateDto,
} from "../dtos/calendar-lock.dto";
import {
  CalendarLockDayInterface,
  CalendarLockInterface,
} from "../interfaces/calendar-lock.interface";

export class CalendarLockSerializer {
  static toCreateDto(
    calendarLock: CalendarLockInterface,
  ): CalendarLockCreateDto {
    const calendarLockDto: CalendarLockCreateDto = {
      idLockType: calendarLock.calendarLockType.id,
      mode: calendarLock.mode,
      days: calendarLock.days,
      fullDay: calendarLock.fullDay,
      recurrence: calendarLock.recurrence,
      startDate: calendarLock.startDate,
      endDate: calendarLock.endDate,
      startTime: calendarLock.startTime,
      endTime: calendarLock.endTime,
      observation: calendarLock.observation,
    };

    return calendarLockDto;
  }

  static toUpdateDto(
    calendarLock: CalendarLockDayInterface,
    observation: string,
  ): CalendarLockUpdateDto {
    const calendarLockUpdateDto: CalendarLockUpdateDto = {
      idDentistCalendarLock: calendarLock.id,
      observationUpdate: observation,
    };

    return calendarLockUpdateDto;
  }
}
