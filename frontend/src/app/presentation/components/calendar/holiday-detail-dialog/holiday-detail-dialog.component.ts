import { Component, inject, Inject } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { HolidayInterface } from "../../../../domain/interfaces/calendar.interface";
import { DentistService } from "../../../../services/dentist.service";
import { AuthService } from "../../../../services/auth.service";
import { SnackbarService } from "../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../utils/enums/snackbar-type.enum";
import { HolidayUpdateAvailabilityDtoInterface } from "../../../../domain/dto/holiday.dto";

@Component({
  selector: "app-holiday-detail-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatDividerModule,
    IconsModule,
  ],
  templateUrl: "./holiday-detail-dialog.component.html",
})
export class HolidayDetailDialogComponent {
  holiday: HolidayInterface;
  date: Date;
  dentistHolidayId: number;
  isWorking: boolean = false; // Indica si el feriado ya está configurado para trabajar
  viewType: "month" | "week" | "day" = "month"; // Tipo de vista desde donde se abrió
  private dialog = inject(MatDialog);
  private readonly dentistService = inject(DentistService);
  private readonly authService = inject(AuthService);
  private readonly snackbarService = inject(SnackbarService);

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      holiday: HolidayInterface;
      date: Date;
      dentistHolidayId: number;
      isWorking?: boolean;
      viewType?: "month" | "week" | "day";
    },
    private dialogRef: MatDialogRef<HolidayDetailDialogComponent>,
  ) {
    this.holiday = data.holiday;
    this.date = data.date;
    this.dentistHolidayId = data.dentistHolidayId;
    // Si el badge es "Disponible", significa que ya está configurado para trabajar
    this.isWorking = data.isWorking || false;
    this.viewType = data.viewType || "month";
  }

  /**
   * Format date to display
   */
  formatDate(date: Date): string {
    return date.toLocaleDateString("es-AR", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }

  /**
   * Open work on holiday dialog
   */
  openWorkOnHolidayDialog(): void {
    import("../work-on-holiday-dialog/work-on-holiday-dialog.component").then(
      (module) => {
        const workDialogRef = this.dialog.open(
          module.WorkOnHolidayDialogComponent,
          {
            width: "800px",
            maxWidth: "90vw",
            data: {
              holiday: this.holiday,
              date: this.date,
            },
          },
        );

        workDialogRef.afterClosed().subscribe((result) => {
          if (result?.configured) {
            // Cerrar el diálogo de detalle y notificar que se configuró
            this.dialogRef.close({ workConfigured: true });
          }
        });
      },
    );
  }

  /**
   * Open modify holiday hours dialog
   */
  openModifyHoursDialog(): void {
    import("../modify-holiday-hours-dialog/modify-holiday-hours-dialog.component").then(
      (module) => {
        const modifyDialogRef = this.dialog.open(
          module.ModifyHolidayHoursDialogComponent,
          {
            width: "600px",
            maxWidth: "90vw",
            data: {
              holiday: this.holiday,
              date: this.date,
              dentistHolidayId: this.dentistHolidayId,
            },
          },
        );

        modifyDialogRef.afterClosed().subscribe((result) => {
          if (result?.updated) {
            // Cerrar el diálogo de detalle y notificar que se actualizó
            this.dialogRef.close({ workConfigured: true });
          }
        });
      },
    );
  }

  /**
   * Stop working on this holiday
   */
  stopWorkingOnHoliday(): void {
    const userData = this.authService.getUserData();
    if (!userData) {
      this.snackbarService.openSnackbar(
        "Error: Usuario no autenticado",
        6000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error,
      );
      return;
    }

    // Build DTO with empty time range and enabled = false
    const dto: HolidayUpdateAvailabilityDtoInterface = {
      idDentistHoliday: this.dentistHolidayId,
      startTime: "00:00",
      endTime: "00:00",
      enabled: false,
    };

    this.dentistService
      .updateAvailabilityHoliday(userData.idUser, dto)
      .subscribe({
        next: (response) => {
          this.snackbarService.openSnackbar(
            response.message,
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Success,
          );
          this.dialogRef.close({ workConfigured: true });
        },
        error: (error) => {
          // Error handling is done by the interceptor
        },
      });
  }
}
