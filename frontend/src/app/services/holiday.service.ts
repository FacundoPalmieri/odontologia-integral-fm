import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../domain/interfaces/api-response.interface";
import { HolidayInterface } from "../domain/interfaces/holiday.interface";

@Injectable({ providedIn: "root" })
export class HolidayService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(
    page: number = 0,
    size: number = 100,
    sortBy: string = "",
    direction: string = "asc"
  ): Observable<ApiResponseInterface<PagedDataInterface<HolidayInterface[]>>> {
    let params = new HttpParams()
      .set("year", 2025)
      .set("page", page)
      .set("size", size)
      .set("sortBy", sortBy)
      .set("direction", direction);

    return this.http.get<
      ApiResponseInterface<PagedDataInterface<HolidayInterface[]>>
    >(`${this.apiUrl}/holiday/all`, { params });
  }

  create(
    holiday: Omit<HolidayInterface, "id">
  ): Observable<ApiResponseInterface<HolidayInterface>> {
    return this.http.post<ApiResponseInterface<HolidayInterface>>(
      `${this.apiUrl}/holiday`,
      holiday
    );
  }

  update(
    holiday: HolidayInterface
  ): Observable<ApiResponseInterface<HolidayInterface>> {
    return this.http.patch<ApiResponseInterface<HolidayInterface>>(
      `${this.apiUrl}/holiday`,
      holiday
    );
  }
}
