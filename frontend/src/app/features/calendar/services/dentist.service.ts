import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { Observable } from "rxjs";
import { DentistDto } from "../domain/dtos/dentist.dto";

/**
 * Service for managing dentist data.
 *
 * This service provides methods to retrieve information about dentists
 * registered in the system.
 */
@Injectable({ providedIn: "root" })
export class DentistService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all dentists in the system.
   *
   * Returns a list of all registered dentists with their basic information
   * including name, specialty, and contact details.
   *
   * @returns Observable with array of dentist data
   */
  getAll(): Observable<ApiResponseInterface<DentistDto[]>> {
    return this.http.get<ApiResponseInterface<DentistDto[]>>(
      `${this.apiUrl}/dentist/all`,
    );
  }
}
