import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  DentistAvailabilityResponseInterface,
  DentistAvailabilitySaveResponseInterface,
  DentistDayAvailabilityInterface,
} from "../data/interfaces/dentist-availability.interface";
import { DentistAvailabilitySerializer } from "../data/serializers/dentist-availability.serializer";

/**
 * Service for managing dentist weekly availability schedules.
 *
 * This service handles the configuration of a dentist's regular weekly schedule,
 * including working days, time slots, and availability patterns.
 * It also provides conflict preview functionality when updating schedules.
 */
@Injectable({ providedIn: "root" })
export class DentistAvailabilityService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves the current availability schedule for a dentist.
   *
   * Returns the dentist's weekly schedule including working days,
   * time slots, and any configured patterns.
   *
   * @param dentistId - The ID of the dentist
   * @returns Observable with the dentist's availability configuration
   */
  get(
    dentistId: number,
  ): Observable<ApiResponseInterface<DentistAvailabilityResponseInterface>> {
    return this.http
      .get<
        ApiResponseInterface<any>
      >(`${this.apiUrl}/dentist-availability/${dentistId}`)
      .pipe(
        map((response) => {
          const days = DentistAvailabilitySerializer.toView(response.data);
          return {
            ...response,
            data: {
              idDentist: response.data?.idDentist || dentistId,
              days,
            },
          };
        }),
      );
  }

  /**
   * Updates the dentist's availability schedule.
   *
   * Saves changes to the dentist's weekly schedule including modifications
   * to working days, time slots, and availability patterns.
   *
   * @param dentistId - The ID of the dentist
   * @param days - Array of day availability configurations
   * @returns Observable with the save operation response
   */
  update(
    dentistId: number,
    days: DentistDayAvailabilityInterface[],
  ): Observable<ApiResponseInterface<any>> {
    const daysDto = DentistAvailabilitySerializer.toDto(days);

    return this.http.patch<
      ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
    >(`${this.apiUrl}/dentist-availability/${dentistId}`, daysDto);
  }

  /**
   * Previews potential conflicts before updating availability.
   *
   * Checks if the proposed schedule changes would conflict with existing
   * appointments or other calendar constraints. This allows users to see
   * potential issues before committing the changes.
   *
   * @param dentistId - The ID of the dentist
   * @param days - Array of proposed day availability configurations
   * @returns Observable with conflict preview data
   */
  previewConflicts(
    dentistId: number,
    days: DentistDayAvailabilityInterface[],
  ): Observable<ApiResponseInterface<any>> {
    const daysDto = DentistAvailabilitySerializer.toDto(days);

    return this.http.patch<
      ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
    >(`${this.apiUrl}/dentist-availability/${dentistId}/preview`, daysDto);
  }
}
