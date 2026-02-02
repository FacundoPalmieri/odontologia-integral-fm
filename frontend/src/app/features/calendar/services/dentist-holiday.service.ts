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

@Injectable({ providedIn: "root" })
export class DentistHolidayService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  save(
    userId: number,
    availability: HolidayWorkConfigDto,
  ): Observable<ApiResponseInterface<HolidayWorkConfigCreateResponseDto>> {
    return this.http.post<
      ApiResponseInterface<HolidayWorkConfigCreateResponseDto>
    >(`${this.apiUrl}/dentist-holiday/${userId}`, availability);
  }

  update(
    userId: number,
    availability: HolidayUpdateAvailabilityDto,
  ): Observable<ApiResponseInterface<HolidayWorkConfigCreateResponseDto>> {
    return this.http.patch<
      ApiResponseInterface<HolidayWorkConfigCreateResponseDto>
    >(`${this.apiUrl}/dentist-holiday/${userId}`, availability);
  }
}
