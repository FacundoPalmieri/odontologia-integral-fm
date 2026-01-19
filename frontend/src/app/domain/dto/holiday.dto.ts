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
  startTime: string;
  endTime: string;
  appointmentDuration: number;
  breakStartTime: string | null;
  breakEndTime: string | null;
}

export interface HolidayWorkConfigCreateResponseDtoInterface {
  id: number;
  idDentist: number;
  idHoliday: number;
  date: string;
  name: string;
  startTime: TimeInterface;
  endTime: TimeInterface;
  appointmentDuration: number;
  enabled: boolean;
}

export interface TimeInterface {
  hour: number;
  minute: number;
  second: number;
  nano: number;
}

export interface HolidayUpdateAvailabilityDtoInterface {
  idDentistHoliday: number;
  startTime: string;
  endTime: string;
  enabled: boolean;
}
