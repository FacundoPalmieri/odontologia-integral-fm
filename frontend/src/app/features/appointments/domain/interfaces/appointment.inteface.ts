import { RequestSourceEnum } from "../../../../shared/utils/enums/request-source.enum";
import { PersonInterface } from "../../../../shared/interfaces/person.interface";
import { PatientInterface } from "../../../patients/domain/interfaces/patient.interface";

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
