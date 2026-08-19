import { Component, inject, Inject } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
  MatDialog,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";

import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { HolidayInterface } from "../../../../holidays/data/interfaces/holiday.interface";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { HolidayUpdateAvailabilityDto } from "../../../data/dtos/calendar-holiday.dto";
import { DentistHolidayService } from "../../../services/dentist-holiday.service";
import { WorkOnHolidayDialogComponent } from "../work-on-holiday-dialog/work-on-holiday-dialog.component";
import { ModifyHolidayHoursDialogComponent } from "../modify-holiday-hours-dialog/modify-holiday-hours-dialog.component";
import { LocalStorageService } from "../../../../../shared/services/local-storage.service";

@Component({
  selector: "app-holiday-detail-dialog",
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatDividerModule,
    IconsModule
],
  templateUrl: "./holiday-detail-dialog.component.html",
})
export class HolidayDetailDialogComponent {
  holiday: HolidayInterface;
  date: Date;
  dentistHolidayId: number;
  isWorking: boolean = false;
  viewType: "month" | "week" | "day" = "month";
  private dialog = inject(MatDialog);
  private readonly dentistHolidayService = inject(DentistHolidayService);
  private readonly localStorageService = inject(LocalStorageService);
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
    const workDialogRef = this.dialog.open(WorkOnHolidayDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      data: {
        holiday: this.holiday,
        date: this.date,
      },
    });

    workDialogRef.afterClosed().subscribe((result) => {
      if (result?.configured) {
        this.dialogRef.close({ workConfigured: true });
      }
    });
  }

  /**
   * Open modify holiday hours dialog
   */
  openModifyHoursDialog(): void {
    const modifyDialogRef = this.dialog.open(
      ModifyHolidayHoursDialogComponent,
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
        this.dialogRef.close({ workConfigured: true });
      }
    });
  }

  /**
   * Stop working on this holiday
   */
  stopWorkingOnHoliday(): void {
    const userData = this.localStorageService.getUserData();
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

    const dto: HolidayUpdateAvailabilityDto = {
      idDentistHoliday: this.dentistHolidayId,
      startTime: "00:00",
      endTime: "00:00",
      enabled: false,
    };

    this.dentistHolidayService.update(userData.idUser, dto).subscribe({
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
    });
  }
}
