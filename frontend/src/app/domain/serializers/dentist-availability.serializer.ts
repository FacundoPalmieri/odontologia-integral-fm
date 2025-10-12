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

    return daysArray.map((dayDto: DentistDayAvailabilityDtoInterface) => ({
      dayName: dayDto.dayName,
      startTime: this.parseTime(dayDto.startTime),
      endTime: this.parseTime(dayDto.endTime),
      appointmentDuration: dayDto.appointmentDuration,
    }));
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
      dayName: day.dayName,
      startTime: this.formatTime(day.startTime),
      endTime: this.formatTime(day.endTime),
      appointmentDuration: day.appointmentDuration,
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
