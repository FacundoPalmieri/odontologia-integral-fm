import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import {
  CalendarLockInterface,
  CalendarLockTypeInterface,
} from "../domain/interfaces/calendar.interface";
import { CalendarLockSerializer } from "../domain/serializers/calendar-lock.serializer";

@Injectable({ providedIn: "root" })
export class CalendarService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getCalendarLockTypes(): Observable<
    ApiResponseInterface<CalendarLockTypeInterface[]>
  > {
    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface[]>>(
      `${this.apiUrl}/calendar-lock-type/all`
    );
  }

  createCalendarLock(
    calendarLock: CalendarLockInterface,
    idPerson: number
  ): Observable<ApiResponseInterface<any>> {
    return this.http.post<ApiResponseInterface<any>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}`,
      CalendarLockSerializer.toCreateDto(calendarLock)
    );
  }

  getMonth(
    idDentist: number,
    date: Date
  ): Observable<ApiResponseInterface<CalendarLockTypeInterface[]>> {
    const params = new HttpParams().set("date", date.toISOString());

    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface[]>>(
      `${this.apiUrl}/${idDentist}/month`,
      { params }
    );
  }

  getWeek(
    idDentist: number,
    date: Date
  ): Observable<ApiResponseInterface<CalendarLockTypeInterface[]>> {
    const params = new HttpParams().set("date", date.toISOString());

    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface[]>>(
      `${this.apiUrl}/${idDentist}/week`,
      { params }
    );
  }

  getDay(
    idDentist: number,
    date: Date
  ): Observable<ApiResponseInterface<CalendarLockTypeInterface[]>> {
    const params = new HttpParams().set("date", date.toISOString());

    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface[]>>(
      `${this.apiUrl}/${idDentist}/day`,
      { params }
    );
  }
}
