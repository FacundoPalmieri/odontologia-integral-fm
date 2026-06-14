export interface ConsultationResponse {
  appappointmentId: number;
  consultationStatus: string;
  dateTime: Date;
  dentistName: string;
  id: number;
  patientName: string;
  patientId: number;
  webSocketStatus: ConsultationWebSocketStatusEnum;
}

export interface ConsultationWebSocketStatusEnum {
  PATIENT_RECEIVED: "PATIENT_RECEIVED";
  ATTENTION_STARTED: "ATTENTION_STARTED";
  ATTENTION_FINISHED: "ATTENTION_FINISHED";
}
