import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { PrestationDto, PrestationStepDto } from "../data/interfaces/prestation.interface";

/**
 * Service for managing dental prestations and prestation instances.
 *
 * This service handles:
 * - Retrieving all active prestations with current pricing
 * - Fetching next habilitated steps for in-progress prestation instances
 */
@Injectable({
  providedIn: "root",
})
export class PrestationService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Lists all active prestations with their current valid price.
   *
   * @returns Observable with API response containing active prestations
   */
  getAll(): Observable<ApiResponseInterface<PrestationDto[]>> {
    return this.http.get<ApiResponseInterface<PrestationDto[]>>(
      `${this.apiUrl}/prestation/all`,
    );
  }

  /**
   * Retrieves the next enabled steps for a prestation instance in progress.
   *
   * @param id Prestation instance identifier
   * @returns Observable with API response containing the next steps
   */
  getNextSteps(id: number | string): Observable<ApiResponseInterface<PrestationStepDto[]>> {
    return this.http.get<ApiResponseInterface<PrestationStepDto[]>>(
      `${this.apiUrl}/prestation-instance/${id}/next-steps`,
    );
  }
}