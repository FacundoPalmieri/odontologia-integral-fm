import { Component, OnDestroy, signal, output } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { Subject } from "rxjs";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatTableModule } from "@angular/material/table";
import { AppointmentConflictInterface } from "../../../domain/interfaces/appointment.inteface";

@Component({
  selector: "app-appointment-conflict",
  templateUrl: "./appointment-conflict.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatCardModule,
    MatButtonModule,
    MatTooltipModule,
    MatTableModule,
  ],
})
export class AppointmentConflictComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();

  goToCalendar = output<void>();

  // TODO - waiting for creating the calendar component
  viewMode = signal<"grid" | "list">("grid");

  // Mock conflicts data (replace with actual data from service)
  conflicts = signal<AppointmentConflictInterface[]>([
    {
      appointmentId: 1,
      appointmentDateTime: new Date("2025-10-05T09:00:00"),
      patientName: "Juan Pérez",
      reasonKey: "DOUBLE_BOOKING",
      reasonLabel: "Doble reserva en el mismo horario",
    },
    {
      appointmentId: 2,
      appointmentDateTime: new Date("2025-10-05T14:30:00"),
      patientName: "María González",
      reasonKey: "OUTSIDE_HOURS",
      reasonLabel: "Turno fuera del horario laboral",
    },
    {
      appointmentId: 3,
      appointmentDateTime: new Date("2025-10-06T10:00:00"),
      patientName: "Carlos López",
      reasonKey: "HOLIDAY",
      reasonLabel: "Turno en día feriado",
    },
  ]);

  displayedColumns = ["patientName", "date", "reason"];

  constructor() {}

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  getReasonColor(reasonKey: string): string {
    const colors: { [key: string]: string } = {
      DOUBLE_BOOKING: "bg-[#f56565]",
      OUTSIDE_HOURS: "bg-[#fb923c]",
      HOLIDAY: "bg-[#f9c20a]",
    };
    return colors[reasonKey] || "bg-[#635bff]";
  }

  getReasonIcon(reasonKey: string): string {
    const icons: { [key: string]: string } = {
      DOUBLE_BOOKING: "calendar-x",
      OUTSIDE_HOURS: "clock-x",
      HOLIDAY: "calendar-event",
    };
    return icons[reasonKey] || "alert-circle";
  }
}
