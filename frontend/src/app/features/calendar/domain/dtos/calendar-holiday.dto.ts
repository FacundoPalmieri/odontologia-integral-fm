export interface HolidayWorkConfigDto {
  year: number;
  idHoliday: number;
  startTime: string;
  endTime: string;
  appointmentDuration: number;
  breakStartTime: string | null;
  breakEndTime: string | null;
}

export interface HolidayWorkConfigCreateResponseDto {
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

export interface HolidayUpdateAvailabilityDto {
  idDentistHoliday: number;
  startTime: string;
  endTime: string;
  enabled: boolean;
}

interface TimeInterface {
  hour: number;
  minute: number;
  second: number;
  nano: number;
}
