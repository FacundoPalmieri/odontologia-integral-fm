import { AppointmentInterface } from "../interfaces/appointment.inteface";
import {
  AppointmentCancelDto,
  AppointmentCreateDto,
  AppointmentRescheduledDto,
} from "../dtos/appointment.dto";

export class AppointmentSerializer {
  static toCreateDto(appointment: AppointmentInterface): AppointmentCreateDto {
    const date = appointment.dateTime;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    const seconds = String(date.getSeconds()).padStart(2, "0");

    const dateTimeString: string = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;

    const appointmentDto: AppointmentCreateDto = {
      idDentist: appointment.dentist.id!,
      idPatient: appointment.patient.person.id!,
      dateTime: dateTimeString,
    };

    return appointmentDto;
  }

  static toCancelDto(appointment: AppointmentInterface): AppointmentCancelDto {
    const appointmentDto: AppointmentCancelDto = {
      requestSource: appointment.requestSource!,
      observation: appointment.observation!,
    };

    return appointmentDto;
  }

  static toRescheduledDto(
    appointment: AppointmentInterface,
  ): AppointmentRescheduledDto {
    const appointmentRescheduledDto: AppointmentRescheduledDto = {
      appointment: this.toCreateDto(appointment),
      requestSource: appointment.requestSource!,
      observation: appointment.observation!,
    };

    return appointmentRescheduledDto;
  }
}
