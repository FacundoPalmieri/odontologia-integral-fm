import {
  HolidayCreateDtoInterface,
  HolidayUpdateDtoInterface,
} from "../dto/holiday.dto";
import { HolidayInterface } from "../interfaces/holiday.interface";
import { HolidayTypeFactory } from "../../utils/factories/holiday-type.factory";

export class HolidaySerializer {
  static toDto<T extends HolidayCreateDtoInterface | HolidayUpdateDtoInterface>(
    holiday: HolidayInterface | Omit<HolidayInterface, "id">,
    includeId: boolean = false
  ): T {
    const baseDto = {
      date: holiday.date,
      name: holiday.name,
      type: holiday.type.value,
    };

    if (includeId && "id" in holiday) {
      return { ...baseDto, id: holiday.id } as T;
    }

    return baseDto as T;
  }

  static toUpdateDto(holiday: HolidayInterface): HolidayUpdateDtoInterface {
    return this.toDto<HolidayUpdateDtoInterface>(holiday, true);
  }

  static toCreateDto(
    holiday: Omit<HolidayInterface, "id">
  ): HolidayCreateDtoInterface {
    return this.toDto<HolidayCreateDtoInterface>(holiday, false);
  }

  static toView(
    holidayDto:
      | HolidayUpdateDtoInterface
      | (HolidayCreateDtoInterface & { id: number })
  ): HolidayInterface {
    const availableTypes = HolidayTypeFactory.createHolidayTypes();

    const holidayType = availableTypes.find(
      (type) => type.label === holidayDto.type
    );

    if (!holidayType) {
      throw new Error(`Tipo de feriado no válido: ${holidayDto.type}`);
    }

    const holidayView: HolidayInterface = {
      id: holidayDto.id,
      name: holidayDto.name,
      date: holidayDto.date,
      type: holidayType,
    };

    return holidayView;
  }
}
