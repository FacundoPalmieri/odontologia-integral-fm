import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { map, Observable } from "rxjs";
import {
  DentistAvailabilityInterface,
  DentistAvailabilitySaveResponseInterface,
  DentistDayAvailabilityInterface,
} from "../domain/interfaces/dentist.interface";
import { DentistAvailabilitySerializer } from "../domain/serializers/dentist-availability.serializer";
import { DentistAvailabilityDtoInterface } from "../domain/dto/dentist.dto";

@Injectable({ providedIn: "root" })
export class DentistService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getDentistAvailability(
    dentistId: number
  ): Observable<ApiResponseInterface<DentistAvailabilityInterface>> {
    return this.http
      .get<ApiResponseInterface<any>>(
        `${this.apiUrl}/dentist-availability/${dentistId}`
      )
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
        })
      );
  }

  saveDentistAvailability(
    dentistId: number,
    days: DentistDayAvailabilityInterface[]
  ): Observable<ApiResponseInterface<any>> {
    const daysDto = DentistAvailabilitySerializer.toDto(days);

    return this.http.patch<
      ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
    >(`${this.apiUrl}/dentist-availability/${dentistId}`, daysDto);
  }
}
