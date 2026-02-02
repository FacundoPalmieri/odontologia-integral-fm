import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  CalendarLockDayInterface,
  CalendarLockInterface,
  CalendarLockTypeInterface,
  CalendarLockTypeModeInterface,
} from "../domain/interfaces/calendar-lock.interface";
import { CalendarLockSerializer } from "../domain/serializers/calendar-lock.serializer";

@Injectable({ providedIn: "root" })
export class CalendarLockService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAllLockTypes(): Observable<
    ApiResponseInterface<CalendarLockTypeInterface[]>
  > {
    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface[]>>(
      `${this.apiUrl}/calendar-lock-type/all`,
    );
  }

  getLockTypeById(
    id: number,
  ): Observable<ApiResponseInterface<CalendarLockTypeInterface>> {
    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface>>(
      `${this.apiUrl}/calendar-lock-type/${id}`,
    );
  }

  getAllCalendarLocksModes(): Observable<
    ApiResponseInterface<CalendarLockTypeModeInterface[]>
  > {
    return this.http.get<ApiResponseInterface<CalendarLockTypeModeInterface[]>>(
      `${this.apiUrl}/calendar-lock-type/mode`,
    );
  }

  createCalendarLockPreview(
    calendarLock: CalendarLockInterface,
    idPerson: number,
  ): Observable<ApiResponseInterface<any>> {
    return this.http.post<ApiResponseInterface<any>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}/preview`,
      CalendarLockSerializer.toCreateDto(calendarLock),
    );
  }

  create(
    calendarLock: CalendarLockInterface,
    idPerson: number,
  ): Observable<ApiResponseInterface<CalendarLockInterface>> {
    return this.http.post<ApiResponseInterface<CalendarLockInterface>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}`,
      CalendarLockSerializer.toCreateDto(calendarLock),
    );
  }

  update(
    calendarLock: CalendarLockDayInterface,
    observation: string,
    idPerson: number,
  ): Observable<ApiResponseInterface<CalendarLockInterface>> {
    return this.http.patch<ApiResponseInterface<CalendarLockInterface>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}`,
      CalendarLockSerializer.toUpdateDto(calendarLock, observation),
    );
  }
}
