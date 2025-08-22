import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { FileMetadataInterface } from "../domain/interfaces/patient.interface";

@Injectable({ providedIn: "root" })
export class FileService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  uploadPatientFile(
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

  getPatientFilesMetadata(
    patientId: number
  ): Observable<ApiResponseInterface<FileMetadataInterface[]>> {
    return this.http.get<ApiResponseInterface<FileMetadataInterface[]>>(
      `${this.apiUrl}/files/patient/all/${patientId}/metadata`
    );
  }

  downloadPatientFile(fileId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/files/patient/${fileId}/download`, {
      responseType: "blob",
    });
  }

  deletePatientFile(fileId: number): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/patient/${fileId}`
    );
  }

  uploadUserFile(
    patientId: number,
    file: File
  ): Observable<ApiResponseInterface<string>> {
    const formData = new FormData();
    formData.append("file", file);
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/user/${patientId}`,
      formData
    );
  }

  getUserFilesMetadata(
    patientId: number
  ): Observable<ApiResponseInterface<FileMetadataInterface[]>> {
    return this.http.get<ApiResponseInterface<FileMetadataInterface[]>>(
      `${this.apiUrl}/files/user/all/${patientId}/metadata`
    );
  }

  downloadUserFile(fileId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/files/user/${fileId}/download`, {
      responseType: "blob",
    });
  }

  deleteUserFile(fileId: number): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/user/${fileId}`
    );
  }
}
