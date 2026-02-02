import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { SystemScheduleInterface } from "../domain/interfaces/system-schedule.interface";
import {
  SystemScheduleDto,
  SystemScheduleUpdateDto,
} from "../domain/dtos/system-schedule.dto";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";

@Injectable({ providedIn: "root" })
export class SystemScheduleService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<SystemScheduleInterface[]>> {
    return this.http.get<ApiResponseInterface<SystemScheduleDto[]>>(
      `${this.apiUrl}/config/all/schedule`,
    );
  }

  update(
    schedule: SystemScheduleUpdateDto,
  ): Observable<ApiResponseInterface<string>> {
    return this.http.patch<ApiResponseInterface<string>>(
      `${this.apiUrl}/config/schedule`,
      schedule,
    );
  }
}
