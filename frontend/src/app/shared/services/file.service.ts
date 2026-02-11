import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../interfaces/api-response.interface";
import { FileMetadataInterface } from "../interfaces/file-metadata.interface";

/**
 * Service for managing file uploads and downloads.
 *
 * This service handles file operations for both patients and users, including:
 * - Uploading files (documents, images, etc.)
 * - Retrieving file metadata
 * - Downloading files
 * - Deleting files
 *
 * Files are associated with either patients or users and stored securely on the server.
 */
@Injectable({ providedIn: "root" })
export class FileService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Uploads a file for a specific patient.
   *
   * @param patientId - The ID of the patient to associate the file with
   * @param file - The file to upload
   * @returns Observable with upload confirmation message
   */
  uploadPatientFile(
    patientId: number,
    file: File,
  ): Observable<ApiResponseInterface<string>> {
    const formData = new FormData();
    formData.append("file", file);
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/patient/${patientId}`,
      formData,
    );
  }

  /**
   * Retrieves metadata for all files associated with a patient.
   *
   * Returns file information such as filename, size, upload date, and type
   * without downloading the actual file content.
   *
   * @param patientId - The ID of the patient
   * @returns Observable with array of file metadata
   */
  getPatientFilesMetadata(
    patientId: number,
  ): Observable<ApiResponseInterface<FileMetadataInterface[]>> {
    return this.http.get<ApiResponseInterface<FileMetadataInterface[]>>(
      `${this.apiUrl}/files/patient/all/${patientId}/metadata`,
    );
  }

  /**
   * Downloads a patient file.
   *
   * Returns the file as a Blob that can be saved or displayed.
   *
   * @param fileId - The ID of the file to download
   * @returns Observable with the file content as a Blob
   */
  downloadPatientFile(fileId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/files/patient/${fileId}/download`, {
      responseType: "blob",
    });
  }

  /**
   * Deletes a patient file.
   *
   * Permanently removes the file from the server.
   *
   * @param fileId - The ID of the file to delete
   * @returns Observable with deletion confirmation message
   */
  deletePatientFile(fileId: number): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/patient/${fileId}`,
    );
  }

  /**
   * Uploads a file for a specific user.
   *
   * @param patientId - The ID of the user to associate the file with (parameter name should be userId but kept for compatibility)
   * @param file - The file to upload
   * @returns Observable with upload confirmation message
   */
  uploadUserFile(
    patientId: number,
    file: File,
  ): Observable<ApiResponseInterface<string>> {
    const formData = new FormData();
    formData.append("file", file);
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/user/${patientId}`,
      formData,
    );
  }

  /**
   * Retrieves metadata for all files associated with a user.
   *
   * Returns file information such as filename, size, upload date, and type
   * without downloading the actual file content.
   *
   * @param patientId - The ID of the user (parameter name should be userId but kept for compatibility)
   * @returns Observable with array of file metadata
   */
  getUserFilesMetadata(
    patientId: number,
  ): Observable<ApiResponseInterface<FileMetadataInterface[]>> {
    return this.http.get<ApiResponseInterface<FileMetadataInterface[]>>(
      `${this.apiUrl}/files/user/all/${patientId}/metadata`,
    );
  }

  /**
   * Downloads a user file.
   *
   * Returns the file as a Blob that can be saved or displayed.
   *
   * @param fileId - The ID of the file to download
   * @returns Observable with the file content as a Blob
   */
  downloadUserFile(fileId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/files/user/${fileId}/download`, {
      responseType: "blob",
    });
  }

  /**
   * Deletes a user file.
   *
   * Permanently removes the file from the server.
   *
   * @param fileId - The ID of the file to delete
   * @returns Observable with deletion confirmation message
   */
  deleteUserFile(fileId: number): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/files/user/${fileId}`,
    );
  }
}
