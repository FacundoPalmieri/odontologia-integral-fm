import { DayEnum } from "../../../../shared/utils/enums/day.enum";

export interface DentistDayAvailabilityDto {
  dayName?: DayEnum | null;
  recurrence?: string | null;
  specificDate: string | null;
  startTime: string;
  endTime: string;
  appointmentDuration: number;
  breakStartTime: string;
  breakEndTime: string;
}

export type DentistAvailabilityDto = DentistDayAvailabilityDto[];
