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

/**
 * Service for managing patient data and medical information.
 *
 * This service handles all patient-related operations including:
 * - CRUD operations for patient records
 * - Retrieving paginated and sorted patient lists
 * - Managing patient medical risks
 * - Data serialization between DTOs and domain models
 */
@Injectable({ providedIn: "root" })
export class PatientService {
  private readonly patientSerializer = new PatientSerializer();
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves a paginated and sorted list of all patients.
   *
   * @param page - Page number (0-indexed), defaults to 0
   * @param size - Number of items per page, defaults to 100
   * @param sortBy - Field name to sort by, defaults to empty string
   * @param direction - Sort direction ('asc' or 'desc'), defaults to 'asc'
   * @returns Observable with paginated patient data
   */
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

  /**
   * Creates a new patient record.
   *
   * Serializes the patient data and sends it to the backend API.
   *
   * @param patient - The patient data to create
   * @returns Observable with the created patient data
   */
  create(
    patient: PatientInterface,
  ): Observable<ApiResponseInterface<PatientDto>> {
    const serializedPatient = this.patientSerializer.toCreateDto(patient);
    return this.http.post<ApiResponseInterface<PatientDto>>(
      `${this.apiUrl}/patient`,
      serializedPatient,
    );
  }

  /**
   * Updates an existing patient record.
   *
   * Serializes the updated patient data and sends it to the backend API.
   *
   * @param patient - The updated patient data
   * @returns Observable with the updated patient data
   */
  update(
    patient: PatientInterface,
  ): Observable<ApiResponseInterface<PatientDto>> {
    const patientSerialized = this.patientSerializer.toCreateDto(patient);
    return this.http.patch<ApiResponseInterface<PatientDto>>(
      `${this.apiUrl}/patient`,
      patientSerialized,
    );
  }

  /**
   * Retrieves a single patient by their ID.
   *
   * Deserializes the response data into the domain model.
   *
   * @param id - The ID of the patient to retrieve
   * @returns Observable with the patient data
   */
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

  /**
   * Updates the medical risks associated with a patient.
   *
   * Medical risks include conditions like diabetes, hypertension, allergies, etc.
   * that may affect dental treatment.
   *
   * @param id - The ID of the patient
   * @param medicalRisks - Array of medical risk data to associate with the patient
   * @returns Observable with confirmation message
   */
  updateMedicalRisks(id: number, medicalRisks: MedicalRiskCreateDto[]) {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/patient/${id}/medical-risk`,
      medicalRisks,
    );
  }
}
