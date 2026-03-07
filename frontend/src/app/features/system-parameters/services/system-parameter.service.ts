import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { SystemParameterInterface } from "../data/interfaces/system-parameter.interface";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  SystemParameterDto,
  SystemParameterUpdateDto,
} from "../data/dtos/system-parameter.dto";
import { environment } from "../../../environments/environment";

/**
 * Service for managing system configuration parameters.
 *
 * System parameters are global configuration settings that affect
 * the behavior of the entire application. This service handles:
 * - Retrieving all system parameters
 * - Updating parameter values
 *
 * Examples of system parameters might include:
 * - Default appointment duration
 * - Business hours
 * - Email notification settings
 * - System-wide preferences
 */
@Injectable({ providedIn: "root" })
export class SystemParameterService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all system configuration parameters.
   *
   * Returns a list of all configurable system parameters with their
   * current values and metadata.
   *
   * @returns Observable with array of system parameter data
   */
  getAll(): Observable<ApiResponseInterface<SystemParameterInterface[]>> {
    return this.http.get<ApiResponseInterface<SystemParameterDto[]>>(
      `${this.apiUrl}/config/system-parameters`,
    );
  }

  /**
   * Updates a system configuration parameter.
   *
   * Modifies the value of a specific system parameter.
   *
   * @param systemParameter - The updated parameter data
   * @returns Observable with confirmation message
   */
  update(
    systemParameter: SystemParameterUpdateDto,
  ): Observable<ApiResponseInterface<string>> {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/system-parameters`,
      systemParameter,
    );
  }
}
