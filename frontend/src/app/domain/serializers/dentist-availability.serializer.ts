import {
  DentistAvailabilityDtoInterface,
  DentistDayAvailabilityDtoInterface,
} from "../dto/dentist.dto";
import {
  DentistDayAvailabilityInterface,
  TimeInterface,
} from "../interfaces/dentist.interface";

export class DentistAvailabilitySerializer {
  /**
   * Convierte los datos de la API al formato que necesita la vista/formulario
   * @param dto Datos de disponibilidad del dentista desde la API
   * @returns Datos formateados para la vista
   */
  static toView(
    dto: DentistAvailabilityDtoInterface | any
  ): DentistDayAvailabilityInterface[] {
    // Manejar el caso donde dto podría ser un objeto con una propiedad days o un array directamente
    const daysArray = Array.isArray(dto) ? dto : dto?.days || [];

    return daysArray.map((dayDto: DentistDayAvailabilityDtoInterface) => {
      const viewData: DentistDayAvailabilityInterface = {
        specificDate: dayDto.specificDate,
        startTime: this.parseTime(dayDto.startTime),
        endTime: this.parseTime(dayDto.endTime),
        appointmentDuration: dayDto.appointmentDuration,
        breakStartTime: dayDto.breakStartTime
          ? this.parseTime(dayDto.breakStartTime)
          : { hour: 0, minute: 0 },
        breakEndTime: dayDto.breakEndTime
          ? this.parseTime(dayDto.breakEndTime)
          : { hour: 0, minute: 0 },
      };

      // Solo incluir dayName y recurrence si están presentes (días semanales)
      if (dayDto.dayName !== undefined && dayDto.dayName !== null) {
        viewData.dayName = dayDto.dayName;
      }
      if (dayDto.recurrence !== undefined && dayDto.recurrence !== null) {
        viewData.recurrence = dayDto.recurrence as any;
      }

      return viewData;
    });
  }

  /**
   * Convierte los datos de la vista/formulario al formato DTO para la API
   * @param days Array de días con disponibilidad del dentista desde el formulario
   * @returns Datos formateados para enviar a la API
   */
  static toDto(
    days: DentistDayAvailabilityInterface[]
  ): DentistDayAvailabilityDtoInterface[] {
    return days.map((day: DentistDayAvailabilityInterface) => ({
      dayName: day.dayName !== undefined ? day.dayName : null,
      recurrence: day.recurrence !== undefined ? day.recurrence : null,
      specificDate:
        day.specificDate === null
          ? null
          : typeof day.specificDate === "string"
          ? day.specificDate
          : day.specificDate.toISOString().split("T")[0],
      startTime: this.formatTime(day.startTime),
      endTime: this.formatTime(day.endTime),
      appointmentDuration: day.appointmentDuration,
      breakStartTime: this.formatTime(day.breakStartTime),
      breakEndTime: this.formatTime(day.breakEndTime),
    }));
  }

  /**
   * Convierte un string de tiempo "HH:MM:SS" a un objeto TimeInterface
   * @param timeString String de tiempo en formato "HH:MM:SS"
   * @returns Objeto TimeInterface con hour y minute
   */
  private static parseTime(timeString: string): TimeInterface {
    const [hour, minute] = timeString.split(":").map(Number);
    return {
      hour,
      minute,
    };
  }

  /**
   * Convierte un objeto TimeInterface a un string de tiempo "HH:MM:SS"
   * @param time Objeto TimeInterface con hour y minute
   * @returns String de tiempo en formato "HH:MM:SS"
   */
  private static formatTime(time: TimeInterface): string {
    const hour = time.hour.toString().padStart(2, "0");
    const minute = time.minute.toString().padStart(2, "0");
    return `${hour}:${minute}:00`;
  }
}
