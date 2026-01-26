import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatTableModule } from "@angular/material/table";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { AppointmentConflictInterface } from "../../../domain/interfaces/appointment.inteface";
import { AppointmentService } from "../../../services/appointment.service";
import { RequestSourceEnum } from "../../../utils/enums/appointment/request-source.enum";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { CancelAppointmentDialog } from "../cancel-appointment-dialog/cancel-appointment-dialog.component";
import { AppointmentCancelDtoInterface } from "../../../domain/dto/appointment.dto";

@Component({
  selector: "app-conflict-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    IconsModule,
  ],
  templateUrl: "./conflict-dialog.component.html",
})
export class ConflictDialogComponent implements OnInit {
  private readonly appointmentService = inject(AppointmentService);
  private readonly dialog = inject(MatDialog);
  private readonly snackbarService = inject(SnackbarService);

  data: {
    conflicts?: AppointmentConflictInterface[];
    dentistId?: number;
    showSaveOption?: boolean;
    allowReschedule?: boolean; // Si es false, solo se puede cancelar (preview mode)
  };
  conflicts: AppointmentConflictInterface[] = [];
  displayedColumns = ["patientName", "date", "origin", "actions"];
  isLoading = false;

  constructor(public dialogRef: MatDialogRef<ConflictDialogComponent>) {
    this.data = inject(MAT_DIALOG_DATA);
  }

  ngOnInit() {
    // Si ya vienen los conflictos, usarlos
    if (this.data.conflicts && this.data.conflicts.length > 0) {
      this.conflicts = this.data.conflicts;
    }
    // Si viene dentistId, cargar los conflictos
    else if (this.data.dentistId) {
      this.loadConflicts(this.data.dentistId);
    }
  }

  loadConflicts(dentistId: number) {
    this.isLoading = true;
    this.appointmentService.getAppointmentConflicts(dentistId).subscribe({
      next: (response) => {
        this.conflicts = response.data || [];
        this.isLoading = false;
      },
      error: (error) => {
        console.error("Error loading conflicts:", error);
        this.conflicts = [];
        this.isLoading = false;
      },
    });
  }

  formatDate(dateTime: Date | string): string {
    try {
      if (!dateTime) return "-";
      const date = typeof dateTime === "string" ? new Date(dateTime) : dateTime;
      if (isNaN(date.getTime())) return "-";
      const day = date.getDate().toString().padStart(2, "0");
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const year = date.getFullYear();
      return `${day}/${month}/${year}`;
    } catch (error) {
      console.error("Error formatting date:", error, dateTime);
      return "-";
    }
  }

  formatTime(dateTime: Date | string): string {
    try {
      if (!dateTime) return "-";
      const date = typeof dateTime === "string" ? new Date(dateTime) : dateTime;
      if (isNaN(date.getTime())) return "-";
      const hours = date.getHours().toString().padStart(2, "0");
      const minutes = date.getMinutes().toString().padStart(2, "0");
      return `${hours}:${minutes}`;
    } catch (error) {
      console.error("Error formatting time:", error, dateTime);
      return "-";
    }
  }

  rescheduleAppointment(conflict: AppointmentConflictInterface) {
    // Importar dinámicamente el componente para evitar dependencias circulares
    import("../calendar/create-appointment-dialog/create-appointment-dialog.component").then(
      (module) => {
        const dialogRef = this.dialog.open(
          module.CreateAppointmentDialogComponent,
          {
            data: {
              idPatient: conflict.idPatient,
              appointmentId: conflict.appointmentId,
            },
          },
        );

        dialogRef.afterClosed().subscribe((result) => {
          if (result?.success) {
            this.snackbarService.openSnackbar(
              "Turno reprogramado exitosamente",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success,
            );
            // Remover el conflicto de la lista ya que fue reprogramado
            this.conflicts = this.conflicts.filter(
              (c) => c.appointmentId !== conflict.appointmentId,
            );
          }
        });
      },
    );
  }

  cancelAppointment(conflict: AppointmentConflictInterface) {
    const dialogRef = this.dialog.open(CancelAppointmentDialog, {
      width: "400px",
      data: { conflict },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.confirmed) {
        this.performCancelAppointment(conflict, result.observation);
      }
    });
  }

  private performCancelAppointment(
    conflict: AppointmentConflictInterface,
    observation: string,
  ) {
    const appointmentData: AppointmentCancelDtoInterface = {
      observation: observation,
      requestSource: RequestSourceEnum.DENTIST,
    };

    this.appointmentService
      .cancel(conflict.appointmentId, appointmentData)
      .subscribe({
        next: (response) => {
          this.snackbarService.openSnackbar(
            response.message,
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Success,
          );
          // Remover el conflicto de la lista
          this.conflicts = this.conflicts.filter(
            (c) => c.appointmentId !== conflict.appointmentId,
          );
        },
      });
  }
}
