import { AppointmentStatusEnum } from "../../utils/enums/appointment/appointment-status.enum";
import { RequestSourceEnum } from "../../utils/enums/appointment/request-source.enum";

export interface AppointmentCreateDtoInterface {
  idDentist: number;
  idPatient: number;
  dateTime: Date;
}

export interface AppointmentCreateResponseDtoInterface {
  id: number;
  dentistName: string;
  patientName: string;
  appointmentDateTime: Date;
  status: AppointmentStatusEnum;
}

export interface AppointmentCancelDtoInterface {
  requestSource: RequestSourceEnum;
  observation?: string;
}

export interface AppointmentRescheduledDtoInterface {
  appointment: AppointmentCreateDtoInterface;
  requestSource: RequestSourceEnum;
  observation: string;
}
