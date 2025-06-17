import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable, of } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { PatientInterface } from "../domain/interfaces/patient.interface";

@Injectable({ providedIn: "root" })
export class PatientService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  private _mockPatients: PatientInterface[] = [];

  getAll(): Observable<ApiResponseInterface<PatientInterface[]>> {
    const response: ApiResponseInterface<PatientInterface[]> = {
      success: true,
      message: "",
      data: this._mockPatients,
    };
    return of(response);
  }

  // create(
  //   patient: PatientCreateDto
  // ): Observable<ApiResponseInterface<PatientCreateDto>> {
  //   return this.http.post<ApiResponseInterface<PatientCreateDto>>(
  //     `${this.apiUrl}/patient`,
  //     patient
  //   );
  // }
}
