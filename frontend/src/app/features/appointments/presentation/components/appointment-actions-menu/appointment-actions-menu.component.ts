import { Component, ChangeDetectionStrategy, inject, input, output } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatMenuModule } from "@angular/material/menu";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { ConsultationService } from "../../../services/consultation.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { Router } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { CreateAppointmentDialogComponent } from "../../../../calendar/presentation/components/create-appointment-dialog/create-appointment-dialog.component";

@Component({
  selector: "app-appointment-actions-menu",
  templateUrl: "./appointment-actions-menu.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
    MatDividerModule,
    IconsModule,
  ],
})
export class AppointmentActionsMenuComponent {
  private readonly consultationService = inject(ConsultationService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);

  appointment = input.required<any>();
  actionPerformed = output<void>();

  viewMedicalHistory() {
    console.log(this.appointment())
    const patientId = this.appointment().patientId;
    if (patientId) {
      this.router.navigate(["/patients/edit", patientId]);
    } else {
      this.snackbarService.openSnackbar(
        "No se encontró el ID del paciente.",
        4000,
        "center",
        "top",
        SnackbarTypeEnum.Error
      );
    }
  }

  rescheduleAppointment() {
    const appointmentId = this.appointment().id;
    const patientId = this.appointment().patientId;

    if (!appointmentId || !patientId) {
      this.snackbarService.openSnackbar(
        "Faltan datos para reprogramar el turno.",
        4000,
        "center",
        "top",
        SnackbarTypeEnum.Error
      );
      return;
    }

    const dialogRef = this.dialog.open(CreateAppointmentDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      data: {
        appointmentId: appointmentId,
        idPatient: patientId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success && result?.isReschedule) {
        this.snackbarService.openSnackbar(
          "Turno reprogramado exitosamente",
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this.actionPerformed.emit();
      }
    });
  }

  startConsultation() {
    this.consultationService.createAppointment(this.appointment().id).subscribe({
      next: () => {
        this.snackbarService.openSnackbar(
          "Paciente pasado a sala de espera.",
          4000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this.actionPerformed.emit();
      },
      error: () => {
        this.snackbarService.openSnackbar(
          "Error al pasar el paciente a sala de espera",
          4000,
          "center",
          "top",
          SnackbarTypeEnum.Error
        );
      }
    });
  }

  callPatient() {
    this.consultationService.callPatient(this.appointment().id).subscribe({
      next: () => {
        this.snackbarService.openSnackbar(
          "Paciente llamado a consulta.",
          4000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this.actionPerformed.emit();
      },
      error: () => {
        this.snackbarService.openSnackbar(
          "Error al llamar al paciente a consulta",
          4000,
          "center",
          "top",
          SnackbarTypeEnum.Error
        );
      }
    });
  }
}
