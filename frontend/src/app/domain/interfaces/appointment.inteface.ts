export interface AppointmentConflictInterface {
  appointmentId: number;
  appointmentDateTime: Date;
  patientName: string;
  reasonKey?: string;
  reasonLabel?: string;
  idOriginConflict?: number;
  nameOriginConflict?: string;
}
