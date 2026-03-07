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
} from "../data/interfaces/calendar-lock.interface";
import { CalendarLockSerializer } from "../data/serializers/calendar-lock.serializer";

/**
 * Service for managing calendar locks and restrictions.
 *
 * Calendar locks are used to block time slots in a dentist's calendar,
 * preventing appointments from being scheduled during specific periods.
 * This service handles:
 * - Retrieving lock types and modes
 * - Creating and updating calendar locks
 * - Previewing lock configurations before applying them
 */
@Injectable({ providedIn: "root" })
export class CalendarLockService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all available calendar lock types.
   *
   * Lock types define the reason for blocking time (e.g., vacation, meeting, personal).
   *
   * @returns Observable with array of lock type configurations
   */
  getAllLockTypes(): Observable<
    ApiResponseInterface<CalendarLockTypeInterface[]>
  > {
    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface[]>>(
      `${this.apiUrl}/calendar-lock-type/all`,
    );
  }

  /**
   * Retrieves a specific lock type by its ID.
   *
   * @param id - The ID of the lock type to retrieve
   * @returns Observable with the lock type configuration
   */
  getLockTypeById(
    id: number,
  ): Observable<ApiResponseInterface<CalendarLockTypeInterface>> {
    return this.http.get<ApiResponseInterface<CalendarLockTypeInterface>>(
      `${this.apiUrl}/calendar-lock-type/${id}`,
    );
  }

  /**
   * Retrieves all available calendar lock modes.
   *
   * Lock modes define how the lock behaves (e.g., recurring, one-time).
   *
   * @returns Observable with array of lock mode configurations
   */
  getAllCalendarLocksModes(): Observable<
    ApiResponseInterface<CalendarLockTypeModeInterface[]>
  > {
    return this.http.get<ApiResponseInterface<CalendarLockTypeModeInterface[]>>(
      `${this.apiUrl}/calendar-lock-type/mode`,
    );
  }

  /**
   * Generates a preview of how a calendar lock will affect the schedule.
   *
   * This allows users to see which time slots will be blocked before confirming.
   *
   * @param calendarLock - The lock configuration to preview
   * @param idPerson - The ID of the person (dentist) whose calendar will be locked
   * @returns Observable with preview data
   */
  createCalendarLockPreview(
    calendarLock: CalendarLockInterface,
    idPerson: number,
  ): Observable<ApiResponseInterface<any>> {
    return this.http.post<ApiResponseInterface<any>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}/preview`,
      CalendarLockSerializer.toCreateDto(calendarLock),
    );
  }

  /**
   * Creates a new calendar lock.
   *
   * Blocks the specified time slots in the dentist's calendar.
   *
   * @param calendarLock - The lock configuration to create
   * @param idPerson - The ID of the person (dentist) whose calendar will be locked
   * @returns Observable with the created lock data
   */
  create(
    calendarLock: CalendarLockInterface,
    idPerson: number,
  ): Observable<ApiResponseInterface<CalendarLockInterface>> {
    return this.http.post<ApiResponseInterface<CalendarLockInterface>>(
      `${this.apiUrl}/dentist-calendar-lock/${idPerson}`,
      CalendarLockSerializer.toCreateDto(calendarLock),
    );
  }

  /**
   * Updates an existing calendar lock.
   *
   * Modifies the time slots or observation for an existing lock.
   *
   * @param calendarLock - The lock day data to update
   * @param observation - Updated observation/note for the lock
   * @param idPerson - The ID of the person (dentist) whose calendar lock is being updated
   * @returns Observable with the updated lock data
   */
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
