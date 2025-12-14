import {
  Component,
  OnDestroy,
  signal,
  output,
  inject,
  input,
  effect,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { Subject, takeUntil } from "rxjs";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatTableModule } from "@angular/material/table";
import { AppointmentConflictInterface } from "../../../domain/interfaces/appointment.inteface";
import { AppointmentService } from "../../../services/appointment.service";
import { Router } from "@angular/router";

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
  private readonly router = inject(Router);
  dentistId = input<number | null>(null);

  private readonly _destroy$ = new Subject<void>();
  private readonly appointmentService = inject(AppointmentService);

  viewMode = signal<"grid" | "list">("grid");

  conflicts = signal<AppointmentConflictInterface[]>([]);

  displayedColumns = ["patientName", "date", "reason"];

  constructor() {
    effect(() => {
      const dentistId = this.dentistId();
      if (dentistId) {
        this.appointmentService
          .getAppointmentConflicts(dentistId)
          .pipe(takeUntil(this._destroy$))
          .subscribe((response) => {
            this.conflicts.set(response.data);
          });
      }
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  goToCalendar() {
    this.router.navigate(["/calendar"]);
  }

  getReasonColor(reasonKey: string): string {
    const colors: { [key: string]: string } = {
      DOUBLE_BOOKING: "bg-[#f56565]",
      OUTSIDE_HOURS: "bg-[#fb923c]",
      HOLIDAY: "bg-[#f9c20a]",
    };
    return colors[reasonKey] || "bg-[#635bff]";
  }

  // getReasonIcon(reasonKey: string): string {
  //   const icons: { [key: string]: string } = {
  //     DOUBLE_BOOKING: "calendar-x",
  //     OUTSIDE_HOURS: "clock-x",
  //     HOLIDAY: "calendar-event",
  //   };
  //   return icons[reasonKey] || "alert-circle";
  // }
}
