import { HolidayTypeEnum } from "../../../../shared/utils/enums/holiday-type.enum";

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
