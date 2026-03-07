import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  TreatmentConditionDto,
  TreatmentDto,
} from "../data/dtos/treatment.dto";

/**
 * Service for managing dental treatments and treatment conditions.
 *
 * This service handles:
 * - Retrieving available dental treatments
 * - Managing treatment conditions (tooth states)
 * - Controlling the treatment sidenav visibility
 */
@Injectable({
  providedIn: "root",
})
export class TreatmentService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Signal controlling the visibility of the treatment sidenav panel.
   */
  sidenavOpen = signal(false);

  /**
   * Retrieves all available dental treatments.
   *
   * Returns a list of all treatments that can be performed,
   * including their names, descriptions, and associated information.
   *
   * @returns Observable with array of treatment data
   */
  getAll(): Observable<ApiResponseInterface<TreatmentDto[]>> {
    return this.http.get<ApiResponseInterface<TreatmentDto[]>>(
      `${this.apiUrl}/treatment/all`,
    );
  }

  /**
   * Retrieves all treatment conditions (tooth states).
   *
   * Treatment conditions represent the various states a tooth can be in
   * (e.g., healthy, cavity, filled, extracted, etc.).
   *
   * @returns Observable with array of treatment condition data
   */
  getAllConditions(): Observable<
    ApiResponseInterface<TreatmentConditionDto[]>
  > {
    return this.http.get<ApiResponseInterface<TreatmentConditionDto[]>>(
      `${this.apiUrl}/treatment-condition/all`,
    );
  }

  /**
   * Toggles the visibility of the treatment sidenav panel.
   *
   * @returns void
   */
  toggleSidenav(): void {
    this.sidenavOpen.set(!this.sidenavOpen());
  }
}
