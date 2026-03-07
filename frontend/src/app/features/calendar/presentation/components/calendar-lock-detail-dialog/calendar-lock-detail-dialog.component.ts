import { Component, inject, Inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { CalendarLockService } from "../../../services/calendar-lock.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { CalendarSlotInterface } from "../../../data/interfaces/calendar.interface";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { UnlockCalendarDialog } from "../unlock-calendar-dialog/unlock-calendar-dialog.component";

@Component({
  selector: "app-calendar-lock-detail-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatDividerModule,
    IconsModule,
  ],
  templateUrl: "./calendar-lock-detail-dialog.component.html",
})
export class CalendarLockDetailDialogComponent {
  private readonly dialog = inject(MatDialog);
  private readonly dialogRef = inject(
    MatDialogRef<CalendarLockDetailDialogComponent>,
  );
  private readonly calendarLockService = inject(CalendarLockService);
  private readonly snackbarService = inject(SnackbarService);

  slot: CalendarSlotInterface;
  idDentist: number;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { slot: CalendarSlotInterface; idDentist: number },
  ) {
    this.slot = data.slot;
    this.idDentist = data.idDentist;
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

  unlockCalendar(): void {
    if (!this.slot.calendarLock) {
      return;
    }

    const dialogRef = this.dialog.open(UnlockCalendarDialog, {
      data: {
        calendarLock: this.slot.calendarLock,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed && result?.observation) {
        this.calendarLockService
          .update(this.slot.calendarLock!, result.observation, this.idDentist)
          .subscribe({
            next: (response) => {
              this.snackbarService.openSnackbar(
                response.message || "Bloqueo eliminado exitosamente",
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success,
              );
              this.dialogRef.close({ unlocked: true });
            },
          });
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
