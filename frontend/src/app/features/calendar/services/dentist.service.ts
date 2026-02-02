import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { Observable } from "rxjs";
import { DentistDto } from "../domain/dtos/dentist.dto";

@Injectable({ providedIn: "root" })
export class DentistService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<DentistDto[]>> {
    return this.http.get<ApiResponseInterface<DentistDto[]>>(
      `${this.apiUrl}/dentist/all`,
    );
  }
}
