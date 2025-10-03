import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { Observable } from "rxjs";
import {
  DentistAvailabilityInterface,
  DentistDayAvailabilityInterface,
} from "../domain/interfaces/dentist.interface";

@Injectable({ providedIn: "root" })
export class DentistService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getDentistAvailability(
    dentistId: number
  ): Observable<ApiResponseInterface<DentistAvailabilityInterface[]>> {
    return this.http.get<ApiResponseInterface<DentistAvailabilityInterface[]>>(
      `${this.apiUrl}/dentist-availability/${dentistId}`
    );
  }

  saveDentistAvailability(
    dentistId: number,
    days: DentistDayAvailabilityInterface[]
  ): Observable<ApiResponseInterface<any>> {
    return this.http.patch<ApiResponseInterface<any>>(
      `${this.apiUrl}/dentist-availability/${dentistId}`,
      days
    );
  }
}
