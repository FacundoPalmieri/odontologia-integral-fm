import { RequestSourceEnum } from "../../../../shared/utils/enums/request-source.enum";
import { AppointmentStatusEnum } from "../../utils/enums/appointment-status.enum";

export interface AppointmentCreateDto {
  idDentist: number;
  idPatient: number;
  dateTime: string;
}

export interface AppointmentCreateResponseDto {
  id: number;
  dentistName: string;
  patientName: string;
  appointmentDateTime: Date;
  status: AppointmentStatusEnum;
}

export interface AppointmentCancelDto {
  requestSource: RequestSourceEnum;
  observation?: string;
}

export interface AppointmentRescheduledDto {
  appointment: AppointmentCreateDto;
  requestSource: RequestSourceEnum;
  observation: string;
}
