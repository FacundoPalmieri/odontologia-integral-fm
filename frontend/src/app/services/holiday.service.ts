import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable, map } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../domain/interfaces/api-response.interface";
import { HolidayInterface } from "../domain/interfaces/holiday.interface";
import { HolidaySerializer } from "../domain/serializers/holiday.serializer";
import { HolidayUpdateDtoInterface } from "../domain/dto/holiday.dto";

@Injectable({ providedIn: "root" })
export class HolidayService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<HolidayInterface[]>> {
    let params = new HttpParams().set("year", 2025);

    return this.http
      .get<ApiResponseInterface<HolidayUpdateDtoInterface[]>>(
        `${this.apiUrl}/holiday/all`,
        { params }
      )
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((holidayDto) =>
            HolidaySerializer.toView(holidayDto)
          ),
        }))
      );
  }

  create(
    holiday: Omit<HolidayInterface, "id">
  ): Observable<ApiResponseInterface<HolidayInterface>> {
    const holidayDto = HolidaySerializer.toCreateDto(holiday);

    return this.http
      .post<ApiResponseInterface<HolidayUpdateDtoInterface>>(
        `${this.apiUrl}/holiday`,
        holidayDto
      )
      .pipe(
        map((response) => ({
          ...response,
          data: HolidaySerializer.toView(response.data),
        }))
      );
  }

  update(
    holiday: HolidayInterface
  ): Observable<ApiResponseInterface<HolidayInterface>> {
    const holidayDto = HolidaySerializer.toUpdateDto(holiday);

    return this.http
      .patch<ApiResponseInterface<HolidayUpdateDtoInterface>>(
        `${this.apiUrl}/holiday`,
        holidayDto
      )
      .pipe(
        map((response) => ({
          ...response,
          data: HolidaySerializer.toView(response.data),
        }))
      );
  }
}
