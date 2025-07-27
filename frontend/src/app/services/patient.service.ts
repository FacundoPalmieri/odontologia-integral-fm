import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { map, Observable } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../domain/interfaces/api-response.interface";
import {
  MedicalRiskCreateDtoInterface,
  PatientDtoInterface,
} from "../domain/dto/patient.dto";
import {
  FileMetadataInterface,
  PatientInterface,
} from "../domain/interfaces/patient.interface";
import { PatientSerializer } from "../domain/serializers/patient.serializer";

@Injectable({ providedIn: "root" })
export class PatientService {
  private readonly patientSerializer = new PatientSerializer();
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(
    page: number = 0,
    size: number = 100,
    sortBy: string = "",
    direction: string = "asc"
  ): Observable<
    ApiResponseInterface<PagedDataInterface<PatientDtoInterface[]>>
  > {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("sortBy", sortBy)
      .set("direction", direction);

    return this.http.get<
      ApiResponseInterface<PagedDataInterface<PatientDtoInterface[]>>
    >(`${this.apiUrl}/patient/all`, { params });
  }

  create(
    user: PatientInterface
  ): Observable<ApiResponseInterface<PatientDtoInterface>> {
    const serializedUser = this.patientSerializer.toCreateDto(user);
    return this.http.post<ApiResponseInterface<PatientDtoInterface>>(
      `${this.apiUrl}/patient`,
      serializedUser
    );
  }

  update(
    patient: PatientInterface
  ): Observable<ApiResponseInterface<PatientDtoInterface>> {
    const patientSerialized = this.patientSerializer.toCreateDto(patient);
    return this.http.patch<ApiResponseInterface<PatientDtoInterface>>(
      `${this.apiUrl}/patient`,
      patientSerialized
    );
  }

  getById(id: number): Observable<ApiResponseInterface<PatientInterface>> {
    return this.http
      .get<ApiResponseInterface<PatientDtoInterface>>(
        `${this.apiUrl}/patient/${id}`
      )
      .pipe(
        map((response) => ({
          ...response,
          data: this.patientSerializer.toView(response.data),
        }))
      );
  }

  updateMedicalRisks(
    id: number,
    medicalRisks: MedicalRiskCreateDtoInterface[]
  ) {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/patient/${id}/medical-risk`,
      medicalRisks
    );
  }

  uploadFile(
    patientId: number,
    file: File
  ): Observable<ApiResponseInterface<string>> {
    const formData = new FormData();
    formData.append("file", file);
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/patient/${patientId}`,
      formData
    );
  }

  getFilesMetadata(
    patientId: number
  ): Observable<ApiResponseInterface<FileMetadataInterface[]>> {
    return this.http.get<ApiResponseInterface<FileMetadataInterface[]>>(
      `${this.apiUrl}/files/patient/all/${patientId}/metadata`
    );
  }

  downloadFile(fileId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/files/patient/${fileId}/download`, {
      responseType: "blob",
    });
  }
}
