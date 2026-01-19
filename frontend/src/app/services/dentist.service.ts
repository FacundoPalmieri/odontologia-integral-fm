import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { map, Observable } from "rxjs";
import {
  DentistAvailabilityResponseInterface,
  DentistAvailabilitySaveResponseInterface,
  DentistDayAvailabilityInterface,
} from "../domain/interfaces/dentist.interface";
import { DentistAvailabilitySerializer } from "../domain/serializers/dentist-availability.serializer";
import { DentistDtoInterface } from "../domain/dto/dentist.dto";
import { DentistHolidayInterface } from "../domain/interfaces/holiday.interface";
import {
  HolidayWorkConfigCreateResponseDtoInterface,
  HolidayWorkConfigDtoInterface,
  HolidayUpdateAvailabilityDtoInterface,
} from "../domain/dto/holiday.dto";

@Injectable({ providedIn: "root" })
export class DentistService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<DentistDtoInterface[]>> {
    return this.http.get<ApiResponseInterface<DentistDtoInterface[]>>(
      `${this.apiUrl}/dentist/all`,
    );
  }

  getAvailability(
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

  previewAvailabilityConflicts(
    dentistId: number,
    days: DentistDayAvailabilityInterface[],
  ): Observable<ApiResponseInterface<any>> {
    const daysDto = DentistAvailabilitySerializer.toDto(days);

    return this.http.patch<
      ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
    >(`${this.apiUrl}/dentist-availability/${dentistId}/preview`, daysDto);
  }

  saveAvailability(
    dentistId: number,
    days: DentistDayAvailabilityInterface[],
  ): Observable<ApiResponseInterface<any>> {
    const daysDto = DentistAvailabilitySerializer.toDto(days);

    return this.http.patch<
      ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
    >(`${this.apiUrl}/dentist-availability/${dentistId}`, daysDto);
  }

  saveAvailabilityHoliday(
    userId: number,
    availability: HolidayWorkConfigDtoInterface,
  ): Observable<
    ApiResponseInterface<HolidayWorkConfigCreateResponseDtoInterface>
  > {
    return this.http.post<
      ApiResponseInterface<HolidayWorkConfigCreateResponseDtoInterface>
    >(`${this.apiUrl}/dentist-holiday/${userId}`, availability);
  }

  updateAvailabilityHoliday(
    userId: number,
    availability: HolidayUpdateAvailabilityDtoInterface,
  ): Observable<
    ApiResponseInterface<HolidayWorkConfigCreateResponseDtoInterface>
  > {
    return this.http.patch<
      ApiResponseInterface<HolidayWorkConfigCreateResponseDtoInterface>
    >(`${this.apiUrl}/dentist-holiday/${userId}`, availability);
  }
}
