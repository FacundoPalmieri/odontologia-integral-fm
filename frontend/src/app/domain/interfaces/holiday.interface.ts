import { HolidayTypeEnum } from "../../utils/enums/day.enum";

export interface HolidayInterface {
  id: number;
  date: Date;
  type: HolidayTypeInterface;
  name: string;
}

export interface HolidayTypeInterface {
  value: HolidayTypeEnum;
  label: string;
}

export interface DentistHolidayInterface {
  idDentist: number;
  holiday: DentistHolidayTimeInterface[];
}

export interface DentistHolidayTimeInterface {
  id: number;
  idHoliday: number;
  startTime: string;
  endTime: string;
}
