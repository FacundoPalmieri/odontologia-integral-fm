import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { map, Observable } from "rxjs";
import { environment } from "../environments/environment";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import {
  TreatmentConditionDtoInterface,
  TreatmentDtoInterface,
} from "../domain/dto/treatment.dto";
import {
  TreatmentConditionInterface,
  TreatmentInterface,
} from "../domain/interfaces/treatment.interface";

@Injectable({
  providedIn: "root",
})
export class TreatmentService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  sidenavOpen = signal(false);

  getAll(): Observable<ApiResponseInterface<TreatmentDtoInterface[]>> {
    return this.http.get<ApiResponseInterface<TreatmentInterface[]>>(
      `${this.apiUrl}/treatment/all`
    );
  }

  getAllConditions(): Observable<
    ApiResponseInterface<TreatmentConditionDtoInterface[]>
  > {
    return this.http.get<ApiResponseInterface<TreatmentConditionInterface[]>>(
      `${this.apiUrl}/treatment-condition/all`
    );
  }

  toggleSidenav(): void {
    this.sidenavOpen.set(!this.sidenavOpen());
  }
}
