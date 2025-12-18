import {
  AppointmentCancelDtoInterface,
  AppointmentCreateDtoInterface,
  AppointmentRescheduledDtoInterface,
} from "../dto/appointment.dto";
import { AppointmentInterface } from "../interfaces/appointment.inteface";

export class AppointmentSerializer {
  static toCreateDto(
    appointment: AppointmentInterface
  ): AppointmentCreateDtoInterface {
    // Formatear la fecha manualmente para evitar conversión a UTC
    const date = appointment.dateTime;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    const seconds = String(date.getSeconds()).padStart(2, "0");

    // Formato: YYYY-MM-DDTHH:mm:ss (sin la Z al final para evitar interpretación UTC)
    const dateTimeString = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;

    const appointmentDto: AppointmentCreateDtoInterface = {
      idDentist: appointment.dentist.id!,
      idPatient: appointment.patient.person.id!,
      dateTime: dateTimeString as any, // El backend espera un string
    };

    return appointmentDto;
  }

  static toCancelDto(
    appointment: AppointmentInterface
  ): AppointmentCancelDtoInterface {
    const appointmentDto: AppointmentCancelDtoInterface = {
      requestSource: appointment.requestSource!,
      observation: appointment.observation!,
    };

    return appointmentDto;
  }

  static toRescheduledDto(
    appointment: AppointmentInterface
  ): AppointmentRescheduledDtoInterface {
    const appointmentRescheduledDto: AppointmentRescheduledDtoInterface = {
      appointment: this.toCreateDto(appointment),
      requestSource: appointment.requestSource!,
      observation: appointment.observation!,
    };

    return appointmentRescheduledDto;
  }
}
