export interface HolidayUpdateDtoInterface {
  id: number;
  name: string;
  date: Date;
  type: string;
}

export interface HolidayCreateDtoInterface {
  name: string;
  date: Date;
  type: string;
}

export interface HolidayWorkConfigDtoInterface {
  year: number;
  idHoliday: number;
  startTime: TimeInterface;
  endTime: TimeInterface;
  appointmentDuration: number;
  breakStartTime: TimeInterface | null;
  breakEndTime: TimeInterface | null;
}

export interface TimeInterface {
  hour: number;
  minute: number;
  second: number;
  nano: number;
}
