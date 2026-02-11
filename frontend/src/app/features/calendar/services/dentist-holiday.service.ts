import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { Observable } from "rxjs";
import {
  HolidayUpdateAvailabilityDto,
  HolidayWorkConfigCreateResponseDto,
  HolidayWorkConfigDto,
} from "../domain/dtos/calendar-holiday.dto";

/**
 * Service for managing dentist holiday work configurations.
 *
 * This service handles the configuration of dentist availability during holidays.
 * Dentists can choose to work on holidays and specify their working hours for those days.
 */
@Injectable({ providedIn: "root" })
export class DentistHolidayService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Saves a new holiday work configuration for a dentist.
   *
   * Creates a new configuration specifying that a dentist will work on a specific holiday
   * and defines their working hours for that day.
   *
   * @param userId - The ID of the user (dentist)
   * @param availability - The holiday work configuration including date and time slots
   * @returns Observable with the created configuration response
   */
  save(
    userId: number,
    availability: HolidayWorkConfigDto,
  ): Observable<ApiResponseInterface<HolidayWorkConfigCreateResponseDto>> {
    return this.http.post<
      ApiResponseInterface<HolidayWorkConfigCreateResponseDto>
    >(`${this.apiUrl}/dentist-holiday/${userId}`, availability);
  }

  /**
   * Updates an existing holiday work configuration.
   *
   * Modifies the working hours or availability status for a previously configured holiday.
   *
   * @param userId - The ID of the user (dentist)
   * @param availability - The updated holiday availability configuration
   * @returns Observable with the updated configuration response
   */
  update(
    userId: number,
    availability: HolidayUpdateAvailabilityDto,
  ): Observable<ApiResponseInterface<HolidayWorkConfigCreateResponseDto>> {
    return this.http.patch<
      ApiResponseInterface<HolidayWorkConfigCreateResponseDto>
    >(`${this.apiUrl}/dentist-holiday/${userId}`, availability);
  }
}
