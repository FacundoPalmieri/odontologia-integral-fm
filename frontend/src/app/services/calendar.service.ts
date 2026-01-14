import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import {
  CalendarDayInterface,
  CalendarLockInterface,
  CalendarLockTypeInterface,
  CalendarLockTypeModeInterface,
  CalendarMonthInterface,
  CalendarWeekInterface,
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

  getCalendarLockTypeById(
    id: number
  ): Observable<ApiResponseInterface<CalendarLockTypeInterface>> {
    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface>>(
      `${this.apiUrl}/calendar-lock-type/${id}`
    );
  }

  getAllCalendarLocksModes(): Observable<
    ApiResponseInterface<CalendarLockTypeModeInterface[]>
  > {
    return this.http.get<ApiResponseInterface<CalendarLockTypeModeInterface[]>>(
      `${this.apiUrl}/calendar-lock-type/mode`
    );
  }

  createCalendarLockPreview(
    calendarLock: CalendarLockInterface,
    idPerson: number
  ): Observable<ApiResponseInterface<any>> {
    return this.http.post<ApiResponseInterface<any>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}/preview`,
      CalendarLockSerializer.toCreateDto(calendarLock)
    );
  }

  createCalendarLock(
    calendarLock: CalendarLockInterface,
    idPerson: number
  ): Observable<ApiResponseInterface<any>> {
    // TODO: Cambiar a CalendarLockInterface
    return this.http.post<ApiResponseInterface<any>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}`,
      CalendarLockSerializer.toCreateDto(calendarLock)
    );
  }

  getMonth(
    idDentist: number,
    year: number,
    month: number
  ): Observable<ApiResponseInterface<CalendarMonthInterface>> {
    const params = new HttpParams()
      .set("year", year.toString())
      .set("month", month.toString());

    return this.http.get<ApiResponseInterface<CalendarMonthInterface>>(
      `${this.apiUrl}/calendar/${idDentist}/month`,
      { params }
    );
  }

  getWeek(
    idDentist: number,
    date: Date
  ): Observable<ApiResponseInterface<CalendarWeekInterface>> {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const formattedDate = `${year}-${month}-${day}`;

    const params = new HttpParams().set("day", formattedDate);

    return this.http.get<ApiResponseInterface<CalendarWeekInterface>>(
      `${this.apiUrl}/calendar/${idDentist}/week`,
      { params }
    );
  }

  getDay(
    idDentist: number,
    date: Date
  ): Observable<ApiResponseInterface<CalendarDayInterface>> {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const formattedDate = `${year}-${month}-${day}`;

    const params = new HttpParams().set("day", formattedDate);

    return this.http.get<ApiResponseInterface<CalendarDayInterface>>(
      `${this.apiUrl}/calendar/${idDentist}/day`,
      { params }
    );
  }
}
