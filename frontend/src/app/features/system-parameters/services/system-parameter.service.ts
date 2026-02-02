import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { SystemParameterInterface } from "../domain/interfaces/system-parameter.interface";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  SystemParameterDto,
  SystemParameterUpdateDto,
} from "../domain/dtos/system-parameter.dto";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class SystemParameterService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<SystemParameterInterface[]>> {
    return this.http.get<ApiResponseInterface<SystemParameterDto[]>>(
      `${this.apiUrl}/config/system-parameters`,
    );
  }

  update(
    systemParameter: SystemParameterUpdateDto,
  ): Observable<ApiResponseInterface<string>> {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/system-parameters`,
      systemParameter,
    );
  }
}
