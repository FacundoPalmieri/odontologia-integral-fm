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
    const appointmentDto: AppointmentCreateDtoInterface = {
      idDentist: appointment.dentist.id!,
      idPatient: appointment.patient.person.id!,
      dateTime: appointment.dateTime,
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
