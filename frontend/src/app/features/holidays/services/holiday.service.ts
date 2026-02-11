import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable, map } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { HolidayInterface } from "../domain/interfaces/holiday.interface";
import { HolidaySerializer } from "../domain/serializers/holiday.serializer";
import { HolidayUpdateDtoInterface } from "../domain/dtos/holiday.dto";

/**
 * Service for managing public holidays.
 *
 * This service handles the management of public holidays in the system,
 * including creating, updating, and retrieving holiday information.
 * Holidays affect dentist availability and appointment scheduling.
 */
@Injectable({ providedIn: "root" })
export class HolidayService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all holidays for the current year.
   *
   * Returns a list of all public holidays configured in the system
   * for the current calendar year.
   *
   * @returns Observable with array of holiday data
   */
  getAll(): Observable<ApiResponseInterface<HolidayInterface[]>> {
    let params = new HttpParams().set(
      "year",
      new Date().getFullYear().toString(),
    );

    return this.http
      .get<
        ApiResponseInterface<HolidayUpdateDtoInterface[]>
      >(`${this.apiUrl}/holiday/all`, { params })
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((holidayDto) =>
            HolidaySerializer.toView(holidayDto),
          ),
        })),
      );
  }

  /**
   * Creates a new holiday.
   *
   * Adds a new public holiday to the system calendar.
   *
   * @param holiday - The holiday data to create (without ID)
   * @returns Observable with the created holiday data
   */
  create(
    holiday: Omit<HolidayInterface, "id">,
  ): Observable<ApiResponseInterface<HolidayInterface>> {
    const holidayDto = HolidaySerializer.toCreateDto(holiday);

    return this.http
      .post<
        ApiResponseInterface<HolidayUpdateDtoInterface>
      >(`${this.apiUrl}/holiday`, holidayDto)
      .pipe(
        map((response) => ({
          ...response,
          data: HolidaySerializer.toView(response.data),
        })),
      );
  }

  /**
   * Updates an existing holiday.
   *
   * Modifies the details of an existing public holiday.
   *
   * @param holiday - The updated holiday data
   * @returns Observable with the updated holiday data
   */
  update(
    holiday: HolidayInterface,
  ): Observable<ApiResponseInterface<HolidayInterface>> {
    const holidayDto = HolidaySerializer.toUpdateDto(holiday);

    return this.http
      .patch<
        ApiResponseInterface<HolidayUpdateDtoInterface>
      >(`${this.apiUrl}/holiday`, holidayDto)
      .pipe(
        map((response) => ({
          ...response,
          data: HolidaySerializer.toView(response.data),
        })),
      );
  }
}
