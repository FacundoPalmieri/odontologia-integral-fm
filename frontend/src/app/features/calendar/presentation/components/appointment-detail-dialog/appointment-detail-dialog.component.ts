import { Component, inject, Inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { MatDialog } from "@angular/material/dialog";
import { Router } from "@angular/router";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { CalendarSlotInterface } from "../../../data/interfaces/calendar.interface";
import { AppointmentService } from "../../../../appointments/services/appointment.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { RequestSourceEnum } from "../../../../../shared/utils/enums/request-source.enum";
import { AppointmentCancelDto } from "../../../../appointments/data/dtos/appointment.dto";
import { CreateAppointmentDialogComponent } from "../create-appointment-dialog/create-appointment-dialog.component";
import { CancelAppointmentDialog } from "../cancel-appointment-dialog/cancel-appointment-dialog.component";

@Component({
  selector: "app-appointment-detail-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatDividerModule,
    IconsModule,
  ],
  templateUrl: "./appointment-detail-dialog.component.html",
})
export class AppointmentDetailDialogComponent {
  private readonly dialog = inject(MatDialog);
  private readonly dialogRef = inject(
    MatDialogRef<AppointmentDetailDialogComponent>,
  );
  private readonly appointmentService = inject(AppointmentService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);

  slot: CalendarSlotInterface;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { slot: CalendarSlotInterface },
  ) {
    this.slot = data.slot;
  }

  formatTime(time: string): string {
    if (!time) return "";
    const [hours, minutes] = time.split(":");
    return `${hours}:${minutes}`;
  }

  formatDate(date: Date | string): string {
    const d = typeof date === "string" ? new Date(date) : date;
    const day = d.getDate().toString().padStart(2, "0");
    const month = (d.getMonth() + 1).toString().padStart(2, "0");
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  }

  getStatusLabel(status: string): string {
    const statusMap: { [key: string]: string } = {
      RESERVED: "Reservado",
      LOCKED: "Bloqueado",
      NOT_AVAILABLE: "No disponible",
      FREE: "Libre",
    };
    return statusMap[status] || status;
  }

  getStatusColor(status: string): string {
    const colorMap: { [key: string]: string } = {
      RESERVED: "var(--mat-sys-primary)",
      LOCKED: "var(--mat-sys-error)",
      NOT_AVAILABLE: "var(--mat-sys-on-surface-variant)",
      FREE: "var(--mat-sys-tertiary)",
    };
    return colorMap[status] || "var(--mat-sys-on-surface)";
  }

  navigateToPatientProfile(): void {
    if (!this.slot.appointment?.idPatient) {
      return;
    }

    this.dialogRef.close();
    this.router.navigate(["/patients/edit", this.slot.appointment.idPatient]);
  }

  rescheduleAppointment(): void {
    if (!this.slot.appointment?.id || !this.slot.appointment?.idPatient) {
      return;
    }

    const dialogRef = this.dialog.open(CreateAppointmentDialogComponent, {
      data: {
        appointmentId: this.slot.appointment.id,
        idPatient: this.slot.appointment.idPatient,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success && result?.isReschedule) {
        this.snackbarService.openSnackbar(
          "Turno reprogramado exitosamente",
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Success,
        );
        this.dialogRef.close({ rescheduled: true });
      }
    });
  }

  cancelAppointment(): void {
    if (!this.slot.appointment?.id) {
      return;
    }

    const dialogRef = this.dialog.open(CancelAppointmentDialog, {
      data: {
        appointmentId: this.slot.appointment.id,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed && result?.observation) {
        const cancelDto: AppointmentCancelDto = {
          requestSource: RequestSourceEnum.DENTIST,
          observation: result.observation,
        };

        this.appointmentService
          .cancel(this.slot.appointment!.id, cancelDto)
          .subscribe({
            next: (response) => {
              this.snackbarService.openSnackbar(
                response.message,
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success,
              );
              this.dialogRef.close({ cancelled: true });
            },
          });
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
