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
