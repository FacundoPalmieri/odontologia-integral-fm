import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { ConsultationResponse } from "../data/interfaces/consultation.interface";

/**
 * Service for managing consultations.
 *
 * This service handles all consultation-related operations including:
 * - Creating consultations from scheduled appointments
 * - Managing the patient workflow (calling patients to consultation)
 */
@Injectable({ providedIn: "root" })
export class ConsultationService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Creates a new consultation record based on an existing appointment.
   * This initializes the consultation workflow for the patient.
   * 
   * @param idAppointment - The ID of the appointment to convert into a consultation
   * @returns Observable with the created consultation details
   */
  createAppointment(idAppointment: number) {
    return this.http.post<ApiResponseInterface<ConsultationResponse>>(
      `${this.apiUrl}/consultation/${idAppointment}`,
      {},
    );
  }

  /**
   * Transitions a patient to the "In Consultation" status.
   * This effectively calls the patient to start their dental visit.
   * 
   * @param idConsultation - The ID of the consultation to update
   * @returns Observable with the updated consultation details
   */
  callPatient(idConsultation: number) {
    return this.http.post<ApiResponseInterface<ConsultationResponse>>(
      `${this.apiUrl}/consultation/${idConsultation}/callPatient`,
      {},
    );
  }

  getConsultations() {
    return this.http.get<ApiResponseInterface<ConsultationResponse[]>>(
      `${this.apiUrl}/consultation`
    );
  }
}
