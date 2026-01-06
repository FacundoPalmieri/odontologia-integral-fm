import { RequestSourceEnum } from "../../utils/enums/appointment/request-source.enum";
import { PatientInterface } from "./patient.interface";
import { PersonInterface } from "./person.interface";

export interface AppointmentInterface {
  patient: PatientInterface;
  dentist: PersonInterface;
  dateTime: Date;
  requestSource?: RequestSourceEnum;
  observation?: string;
}

export interface AppointmentConflictInterface {
  appointmentId: number;
  appointmentDateTime: Date;
  idPatient: number;
  patientName: string;
  idOriginConflict?: number;
  nameOriginConflict?: string;
}
