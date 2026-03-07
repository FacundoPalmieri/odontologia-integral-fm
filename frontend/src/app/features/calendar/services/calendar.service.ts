import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import {
  CalendarDayInterface,
  CalendarMonthInterface,
  CalendarWeekInterface,
} from "../data/interfaces/calendar.interface";

/**
 * Service for retrieving calendar data for dentists.
 *
 * This service provides methods to fetch calendar information in different views:
 * - Monthly view: Overview of an entire month
 * - Weekly view: Detailed view of a specific week
 * - Daily view: Detailed view of a specific day
 *
 * Each view includes appointment slots, availability, and calendar locks.
 */
@Injectable({ providedIn: "root" })
export class CalendarService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves calendar data for a specific month.
   *
   * Returns an overview of the entire month including all appointments,
   * availability, and calendar locks for the specified dentist.
   *
   * @param idDentist - The ID of the dentist
   * @param year - The year (e.g., 2024)
   * @param month - The month (1-12)
   * @returns Observable with monthly calendar data
   */
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

  /**
   * Retrieves calendar data for a specific week.
   *
   * Returns detailed information for the week containing the specified date,
   * including time slots, appointments, and availability.
   *
   * @param idDentist - The ID of the dentist
   * @param date - Any date within the desired week
   * @returns Observable with weekly calendar data
   */
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

  /**
   * Retrieves calendar data for a specific day.
   *
   * Returns detailed information for a single day including all time slots,
   * appointments, and availability status.
   *
   * @param idDentist - The ID of the dentist
   * @param date - The specific date to retrieve
   * @returns Observable with daily calendar data
   */
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
