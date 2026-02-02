import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  TreatmentConditionDto,
  TreatmentDto,
} from "../domain/dtos/treatment.dto";

@Injectable({
  providedIn: "root",
})
export class TreatmentService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  sidenavOpen = signal(false);

  getAll(): Observable<ApiResponseInterface<TreatmentDto[]>> {
    return this.http.get<ApiResponseInterface<TreatmentDto[]>>(
      `${this.apiUrl}/treatment/all`,
    );
  }

  getAllConditions(): Observable<
    ApiResponseInterface<TreatmentConditionDto[]>
  > {
    return this.http.get<ApiResponseInterface<TreatmentConditionDto[]>>(
      `${this.apiUrl}/treatment-condition/all`,
    );
  }

  toggleSidenav(): void {
    this.sidenavOpen.set(!this.sidenavOpen());
  }
}
