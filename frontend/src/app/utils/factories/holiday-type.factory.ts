import { HolidayTypeInterface } from "../../domain/interfaces/holiday.interface";
import { HolidayTypeEnum } from "../enums/day.enum";

export class HolidayTypeFactory {
  static createHolidayTypes(): HolidayTypeInterface[] {
    return [
      { value: HolidayTypeEnum.IMMOVABLE, label: "Inamovible" },
      { value: HolidayTypeEnum.LONG_WEEKEND, label: "Puente" },
      { value: HolidayTypeEnum.MOVEABLE, label: "Trasladable" },
    ];
  }
}
