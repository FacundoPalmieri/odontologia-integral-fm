import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../domain/interfaces/api-response.interface";
import { PatientDtoInterface } from "../domain/dto/patient.dto";
import { PatientInterface } from "../domain/interfaces/patient.interface";
import { PatientSerializer } from "../domain/serializers/patient.serializer";

@Injectable({ providedIn: "root" })
export class PatientService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(
    page: number = 0,
    size: number = 100
  ): Observable<
    ApiResponseInterface<PagedDataInterface<PatientDtoInterface[]>>
  > {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    return this.http.get<
      ApiResponseInterface<PagedDataInterface<PatientDtoInterface[]>>
    >(`${this.apiUrl}/patient/all`, { params });
  }

  create(
    user: PatientInterface
  ): Observable<ApiResponseInterface<PatientDtoInterface>> {
    const serializedUser = PatientSerializer.toCreateDto(user);
    return this.http.post<ApiResponseInterface<PatientDtoInterface>>(
      `${this.apiUrl}/patient`,
      serializedUser
    );
  }

  update(
    patient: PatientInterface
  ): Observable<ApiResponseInterface<PatientDtoInterface>> {
    const patientSerialized = PatientSerializer.toCreateDto(patient);
    return this.http.patch<ApiResponseInterface<PatientDtoInterface>>(
      `${this.apiUrl}/patient`,
      patientSerialized
    );
  }

  getById(id: number): Observable<ApiResponseInterface<PatientDtoInterface>> {
    return this.http.get<ApiResponseInterface<PatientDtoInterface>>(
      `${this.apiUrl}/patient/${id}`
    );
  }
}
