import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  DentistAvailabilityResponseInterface,
  DentistAvailabilitySaveResponseInterface,
  DentistDayAvailabilityInterface,
} from "../domain/interfaces/dentist-availability.interface";
import { DentistAvailabilitySerializer } from "../domain/serializers/dentist-availability.serializer";

@Injectable({ providedIn: "root" })
export class DentistAvailabilityService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

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

  update(
    dentistId: number,
    days: DentistDayAvailabilityInterface[],
  ): Observable<ApiResponseInterface<any>> {
    const daysDto = DentistAvailabilitySerializer.toDto(days);

    return this.http.patch<
      ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
    >(`${this.apiUrl}/dentist-availability/${dentistId}`, daysDto);
  }

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
