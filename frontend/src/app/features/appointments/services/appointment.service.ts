import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import {
  AppointmentConflictInterface,
  AppointmentInterface,
} from "../data/interfaces/appointment.inteface";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { environment } from "../../../environments/environment";
import { AppointmentSerializer } from "../data/serializers/appointment.serializer";
import {
  AppointmentCancelDto,
  AppointmentCreateResponseDto,
} from "../data/dtos/appointment.dto";

/**
 * Service for managing dental appointments.
 *
 * This service handles all appointment-related operations including:
 * - Creating, canceling, and rescheduling appointments
 * - Retrieving appointments by status (scheduled, waiting, in progress, etc.)
 * - Managing appointment conflicts
 * - Bulk cancellation of appointments
 */
@Injectable({ providedIn: "root" })
export class AppointmentService {
  private readonly appointmentSerializer = AppointmentSerializer;
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  scheduled_appointments = [];
  waiting_appointments = [];
  in_progress_appointments = [];
  pending_payment_appointments = [];
  finalized_appointments = [];
  canceled_appointments = [];

  /**
   * Retrieves all appointments (scheduled, waiting, etc.).
   *
   * @returns Array of all appointments (mock data)
   */
  getAll(): any[] {
    return [
      ...this.scheduled_appointments,
      ...this.waiting_appointments,
      ...this.in_progress_appointments,
      ...this.pending_payment_appointments,
      ...this.finalized_appointments,
      ...this.canceled_appointments,
    ];
  }

  /**
   * Retrieves all scheduled appointments.
   *
   * @returns Array of scheduled appointments (mock data)
   */
  getScheduled(): any[] {
    return [...this.scheduled_appointments, ...this.waiting_appointments];
  }


  /**
   * Retrieves all appointments currently in progress.
   *
   * @returns Array of in-progress appointments (mock data)
   */
  getInProgress(): any[] {
    return this.in_progress_appointments;
  }

  /**
   * Retrieves all appointments with pending payment.
   *
   * @returns Array of pending payment appointments (mock data)
   */
  getPendingPayment(): any[] {
    return this.pending_payment_appointments;
  }

  /**
   * Retrieves all finalized appointments.
   *
   * @returns Array of finalized appointments (mock data)
   */
  getFinalized(): any[] {
    return this.finalized_appointments;
  }

  /**
   * Retrieves all canceled appointments.
   *
   * @returns Array of canceled appointments (mock data)
   */
  getCanceled(): any[] {
    return this.canceled_appointments;
  }

  /**
   * Creates a new appointment.
   *
   * Serializes the appointment data and sends it to the backend API.
   *
   * @param appointment - The appointment data to create
   * @returns Observable with the API response containing the created appointment
   */
  create(
    appointment: AppointmentInterface,
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDto>> {
    const serializedAppointment =
      this.appointmentSerializer.toCreateDto(appointment);
    return this.http.post<ApiResponseInterface<AppointmentCreateResponseDto>>(
      `${this.apiUrl}/appointment`,
      serializedAppointment,
    );
  }

  /**
   * Cancels a specific appointment.
   *
   * @param appointmentId - The ID of the appointment to cancel
   * @param appointmentCancelRequest - The cancellation request data
   * @returns Observable with the API response
   */
  cancel(
    appointmentId: number,
    appointmentCancelRequest: AppointmentCancelDto,
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDto>> {
    return this.http.patch<ApiResponseInterface<AppointmentCreateResponseDto>>(
      `${this.apiUrl}/appointment/${appointmentId}/cancel`,
      appointmentCancelRequest,
    );
  }

  /**
   * Cancels all appointments for a specific dentist on a given date.
   *
   * This is useful for bulk cancellation when a dentist is unavailable for an entire day.
   *
   * @param dentistId - The ID of the dentist
   * @param date - The date for which to cancel all appointments
   * @param appointment - The appointment data containing cancellation details
   * @returns Observable with the API response
   */
  cancelAll(
    dentistId: number,
    date: Date,
    appointment: AppointmentInterface,
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDto>> {
    const serializedAppointment =
      this.appointmentSerializer.toCancelDto(appointment);

    const formattedDate =
      date instanceof Date ? date.toISOString().split("T")[0] : date;

    const params: HttpParams = new HttpParams().set("date", formattedDate);

    return this.http.patch<ApiResponseInterface<AppointmentCreateResponseDto>>(
      `${this.apiUrl}/appointment/${dentistId}/all/cancel`,
      serializedAppointment,
      { params },
    );
  }

  /**
   * Reschedules an existing appointment to a new date/time.
   *
   * @param appointmentId - The ID of the appointment to reschedule
   * @param appointment - The new appointment data with updated date/time
   * @returns Observable with the API response
   */
  reschedule(
    appointmentId: number,
    appointment: AppointmentInterface,
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDto>> {
    const serializedAppointment =
      this.appointmentSerializer.toRescheduledDto(appointment);

    return this.http.patch<ApiResponseInterface<AppointmentCreateResponseDto>>(
      `${this.apiUrl}/appointment/${appointmentId}/reschedule`,
      serializedAppointment,
    );
  }

  /**
   * Retrieves all appointment conflicts for a specific dentist.
   *
   * Conflicts occur when appointments overlap or violate scheduling rules.
   *
   * @param dentistId - The ID of the dentist to check for conflicts
   * @returns Observable with array of appointment conflicts
   */
  getAppointmentConflicts(
    dentistId: number,
  ): Observable<ApiResponseInterface<AppointmentConflictInterface[]>> {
    return this.http.get<ApiResponseInterface<AppointmentConflictInterface[]>>(
      `${this.apiUrl}/appointment/conflict/all/${dentistId}`,
    );
  }
}
