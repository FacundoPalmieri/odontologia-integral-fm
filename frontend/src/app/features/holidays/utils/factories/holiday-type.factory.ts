import { HolidayTypeEnum } from "../../../../shared/utils/enums/holiday-type.enum";
import { HolidayTypeInterface } from "../../domain/interfaces/holiday.interface";

export class HolidayTypeFactory {
  static createHolidayTypes(): HolidayTypeInterface[] {
    return [
      { value: HolidayTypeEnum.IMMOVABLE, label: "Inamovible" },
      { value: HolidayTypeEnum.LONG_WEEKEND, label: "Puente" },
      { value: HolidayTypeEnum.MOVEABLE, label: "Trasladable" },
    ];
  }
}
