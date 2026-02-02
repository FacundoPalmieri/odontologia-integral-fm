import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../shared/interfaces/api-response.interface";
import { map, Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { PatientSerializer } from "../domain/serializers/patient.serializer";
import { PatientDto } from "../domain/dtos/patient.dto";
import { PatientInterface } from "../domain/interfaces/patient.interface";
import { MedicalRiskCreateDto } from "../domain/dtos/medical-history.dto";

@Injectable({ providedIn: "root" })
export class PatientService {
  private readonly patientSerializer = new PatientSerializer();
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(
    page: number = 0,
    size: number = 100,
    sortBy: string = "",
    direction: string = "asc",
  ): Observable<ApiResponseInterface<PagedDataInterface<PatientDto[]>>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("sortBy", sortBy)
      .set("direction", direction);

    return this.http.get<
      ApiResponseInterface<PagedDataInterface<PatientDto[]>>
    >(`${this.apiUrl}/patient/all`, { params });
  }

  create(
    patient: PatientInterface,
  ): Observable<ApiResponseInterface<PatientDto>> {
    const serializedPatient = this.patientSerializer.toCreateDto(patient);
    return this.http.post<ApiResponseInterface<PatientDto>>(
      `${this.apiUrl}/patient`,
      serializedPatient,
    );
  }

  update(
    patient: PatientInterface,
  ): Observable<ApiResponseInterface<PatientDto>> {
    const patientSerialized = this.patientSerializer.toCreateDto(patient);
    return this.http.patch<ApiResponseInterface<PatientDto>>(
      `${this.apiUrl}/patient`,
      patientSerialized,
    );
  }

  getById(id: number): Observable<ApiResponseInterface<PatientInterface>> {
    return this.http
      .get<ApiResponseInterface<PatientDto>>(`${this.apiUrl}/patient/${id}`)
      .pipe(
        map((response) => ({
          ...response,
          data: this.patientSerializer.toView(response.data),
        })),
      );
  }

  updateMedicalRisks(id: number, medicalRisks: MedicalRiskCreateDto[]) {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/patient/${id}/medical-risk`,
      medicalRisks,
    );
  }
}
