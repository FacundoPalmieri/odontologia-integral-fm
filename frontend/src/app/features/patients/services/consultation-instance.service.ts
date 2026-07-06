import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { ConsultationInstanceRequest, ConsultationInstanceResponse } from "../data/interfaces/consultation-instance.interface";

/**
 * Service for managing consultation instances.
 *
 * This service handles operations related to consultation instances,
 * including creating new instances and retrieving their details.
 */
@Injectable({ providedIn: "root" })
export class ConsultationInstanceService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Creates a new consultation instance.
   *
   * @param instance - The consultation instance data to create.
   * @returns Observable with the created consultation instance response.
   */
  createConsultationInstance(instance: ConsultationInstanceRequest): Observable<ApiResponseInterface<ConsultationInstanceResponse>> {
    return this.http.post<ApiResponseInterface<ConsultationInstanceResponse>>(
      `${this.apiUrl}/consultation-instance`,
      instance
    );
  }

  /**
   * Retrieves the details of a consultation instance by its ID.
   *
   * @param id - The ID of the consultation instance.
   * @returns Observable with the consultation instance details.
   */
  getConsultationInstance(id: number): Observable<ApiResponseInterface<ConsultationInstanceResponse>> {
    return this.http.get<ApiResponseInterface<ConsultationInstanceResponse>>(
      `${this.apiUrl}/consultation-instance/${id}`
    );
  }
}


