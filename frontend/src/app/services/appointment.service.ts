import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import {
  AppointmentConflictInterface,
  AppointmentInterface,
} from "../domain/interfaces/appointment.inteface";
import {
  AppointmentCreateResponseDtoInterface,
  AppointmentRescheduledDtoInterface,
} from "../domain/dto/appointment.dto";
import { AppointmentSerializer } from "../domain/serializers/appointment.serializer";

@Injectable({ providedIn: "root" })
export class AppointmentService {
  private readonly appointmentSerializer = AppointmentSerializer;
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  scheduled_appointments = [
    {
      firstName: "Lucía",
      lastName: "Pérez",
      appointmentDateTime: "2025-07-29T09:00",
      professional: "Dr. Ana López",
      status: "Agendado",
    },
    {
      firstName: "Florencia",
      lastName: "Acosta",
      appointmentDateTime: "2025-07-29T12:00",
      professional: "Dr. Pablo Méndez",
      status: "Agendado",
    },
    {
      firstName: "Ignacio",
      lastName: "Herrera",
      appointmentDateTime: "2025-07-29T14:30",
      professional: "Dr. Juan Rodríguez",
      status: "Agendado",
    },
  ];
  waiting_appointments = [
    {
      firstName: "Martín",
      lastName: "Gómez",
      appointmentDateTime: "2025-07-29T09:30",
      professional: "Dr. Juan Rodríguez",
      status: "En espera",
    },
    {
      firstName: "Mateo",
      lastName: "Morales",
      appointmentDateTime: "2025-07-29T12:30",
      professional: "Dr. Juan Rodríguez",
      status: "En espera",
    },
    {
      firstName: "Franco",
      lastName: "Vera",
      appointmentDateTime: "2025-07-29T15:30",
      professional: "Dr. Ana López",
      status: "En espera",
    },
  ];
  in_progress_appointments = [
    {
      firstName: "Camila",
      lastName: "Ramírez",
      appointmentDateTime: "2025-07-29T10:00",
      professional: "Dr. Ana López",
      status: "En consulta",
    },
    {
      firstName: "Carla",
      lastName: "Navarro",
      appointmentDateTime: "2025-07-29T13:00",
      professional: "Dr. Ana López",
      status: "En consulta",
    },
    {
      firstName: "Julieta",
      lastName: "Benítez",
      appointmentDateTime: "2025-07-29T16:00",
      professional: "Dr. Juan Rodríguez",
      status: "En consulta",
    },
  ];
  pending_payment_appointments = [
    {
      firstName: "Julián",
      lastName: "Fernández",
      appointmentDateTime: "2025-07-29T10:30",
      professional: "Dr. Pablo Méndez",
      status: "Pendiente de pago",
    },
    {
      firstName: "Micaela",
      lastName: "Luna",
      appointmentDateTime: "2025-07-29T14:00",
      professional: "Dr. Ana López",
      status: "Pendiente de pago",
    },
  ];
  finalized_appointments = [
    {
      firstName: "Valentina",
      lastName: "Silva",
      appointmentDateTime: "2025-07-29T11:00",
      professional: "Dr. Juan Rodríguez",
      status: "Finalizada",
    },
    {
      firstName: "Tomás",
      lastName: "Castro",
      appointmentDateTime: "2025-07-29T13:30",
      professional: "Dr. Pablo Méndez",
      status: "Finalizada",
    },
  ];
  canceled_appointments = [
    {
      firstName: "Santiago",
      lastName: "Díaz",
      appointmentDateTime: "2025-07-29T11:30",
      professional: "Dr. Ana López",
      status: "Cancelada",
    },
    {
      firstName: "Agustina",
      lastName: "Sosa",
      appointmentDateTime: "2025-07-29T15:00",
      professional: "Dr. Pablo Méndez",
      status: "Cancelada",
    },
  ];

  getScheduled(): any[] {
    return this.scheduled_appointments;
  }

  getWaiting(): any[] {
    return this.waiting_appointments;
  }

  getInProgress(): any[] {
    return this.in_progress_appointments;
  }

  getPendingPayment(): any[] {
    return this.pending_payment_appointments;
  }

  getFinalized(): any[] {
    return this.finalized_appointments;
  }

  getCanceled(): any[] {
    return this.canceled_appointments;
  }

  create(
    appointment: AppointmentInterface
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDtoInterface>> {
    const serializedAppointment =
      this.appointmentSerializer.toCreateDto(appointment);
    return this.http.post<
      ApiResponseInterface<AppointmentCreateResponseDtoInterface>
    >(`${this.apiUrl}/appointment`, serializedAppointment);
  }

  cancel(
    appointmentId: number,
    appointment: AppointmentInterface
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDtoInterface>> {
    const serializedAppointment =
      this.appointmentSerializer.toCancelDto(appointment);
    return this.http.post<
      ApiResponseInterface<AppointmentCreateResponseDtoInterface>
    >(
      `${this.apiUrl}/appointment/${appointmentId}/cancel`,
      serializedAppointment
    );
  }

  cancelAll(
    dentistId: number,
    date: Date,
    appointment: AppointmentInterface
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDtoInterface>> {
    const serializedAppointment =
      this.appointmentSerializer.toCancelDto(appointment);

    const formattedDate =
      date instanceof Date ? date.toISOString().split("T")[0] : date;

    const params = new HttpParams().set("date", formattedDate);

    return this.http.post<
      ApiResponseInterface<AppointmentCreateResponseDtoInterface>
    >(
      `${this.apiUrl}/appointment/${dentistId}/all/cancel`,
      serializedAppointment,
      { params }
    );
  }

  reschedule(
    appointmentId: number,
    appointment: AppointmentInterface
  ): Observable<ApiResponseInterface<AppointmentCreateResponseDtoInterface>> {
    const serializedAppointment =
      this.appointmentSerializer.toRescheduledDto(appointment);

    return this.http.post<
      ApiResponseInterface<AppointmentCreateResponseDtoInterface>
    >(
      `${this.apiUrl}/appointment/${appointmentId}/reschedule`,
      serializedAppointment
    );
  }

  getAppointmentConflicts(
    dentistId: number
  ): Observable<ApiResponseInterface<AppointmentConflictInterface[]>> {
    return this.http.get<ApiResponseInterface<AppointmentConflictInterface[]>>(
      `${this.apiUrl}/appointment/conflict/all/${dentistId}`
    );
  }
}
