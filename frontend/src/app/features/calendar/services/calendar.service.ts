import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  CalendarDayInterface,
  CalendarMonthInterface,
  CalendarWeekInterface,
} from "../domain/interfaces/calendar.interface";

@Injectable({ providedIn: "root" })
export class CalendarService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getMonth(
    idDentist: number,
    year: number,
    month: number,
  ): Observable<ApiResponseInterface<CalendarMonthInterface>> {
    const params = new HttpParams()
      .set("year", year.toString())
      .set("month", month.toString());

    return this.http.get<ApiResponseInterface<CalendarMonthInterface>>(
      `${this.apiUrl}/calendar/${idDentist}/month`,
      { params },
    );
  }

  getWeek(
    idDentist: number,
    date: Date,
  ): Observable<ApiResponseInterface<CalendarWeekInterface>> {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const formattedDate = `${year}-${month}-${day}`;

    const params = new HttpParams().set("day", formattedDate);

    return this.http.get<ApiResponseInterface<CalendarWeekInterface>>(
      `${this.apiUrl}/calendar/${idDentist}/week`,
      { params },
    );
  }

  getDay(
    idDentist: number,
    date: Date,
  ): Observable<ApiResponseInterface<CalendarDayInterface>> {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const formattedDate = `${year}-${month}-${day}`;

    const params = new HttpParams().set("day", formattedDate);

    return this.http.get<ApiResponseInterface<CalendarDayInterface>>(
      `${this.apiUrl}/calendar/${idDentist}/day`,
      { params },
    );
  }
}
