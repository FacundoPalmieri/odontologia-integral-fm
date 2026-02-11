import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { SystemScheduleInterface } from "../domain/interfaces/system-schedule.interface";
import {
  SystemScheduleDto,
  SystemScheduleUpdateDto,
} from "../domain/dtos/system-schedule.dto";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";

/**
 * Service for managing system-wide schedule configuration.
 *
 * The system schedule defines the default operating hours and
 * availability patterns for the clinic. This service handles:
 * - Retrieving the system schedule configuration
 * - Updating schedule settings
 *
 * The system schedule serves as the default template for dentist schedules
 * and defines when the clinic is generally open for appointments.
 */
@Injectable({ providedIn: "root" })
export class SystemScheduleService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all system schedule configurations.
   *
   * Returns the system-wide schedule settings including operating hours,
   * days of operation, and default time slot configurations.
   *
   * @returns Observable with array of system schedule data
   */
  getAll(): Observable<ApiResponseInterface<SystemScheduleInterface[]>> {
    return this.http.get<ApiResponseInterface<SystemScheduleDto[]>>(
      `${this.apiUrl}/config/all/schedule`,
    );
  }

  /**
   * Updates the system schedule configuration.
   *
   * Modifies the system-wide schedule settings such as operating hours
   * and default availability patterns.
   *
   * @param schedule - The updated schedule configuration
   * @returns Observable with confirmation message
   */
  update(
    schedule: SystemScheduleUpdateDto,
  ): Observable<ApiResponseInterface<string>> {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/schedule`,
      schedule,
    );
  }
}
