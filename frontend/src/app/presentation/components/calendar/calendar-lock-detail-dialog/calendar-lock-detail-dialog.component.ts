import { Component, inject, Inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { SlotInterface } from "../../../../domain/interfaces/calendar.interface";
import { MatDialog } from "@angular/material/dialog";
import { CalendarService } from "../../../../services/calendar.service";
import { SnackbarService } from "../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../utils/enums/snackbar-type.enum";

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
  private readonly calendarService = inject(CalendarService);
  private readonly snackbarService = inject(SnackbarService);

  slot: SlotInterface;
  idDentist: number;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { slot: SlotInterface; idDentist: number },
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

    // Abrir diálogo de confirmación con observación
    import("../../unlock-calendar-dialog/unlock-calendar-dialog.component").then(
      (module) => {
        const dialogRef = this.dialog.open(module.UnlockCalendarDialog, {
          data: {
            calendarLock: this.slot.calendarLock,
          },
        });

        dialogRef.afterClosed().subscribe((result) => {
          if (result?.confirmed && result?.observation) {
            this.calendarService
              .updateCalendarLock(
                this.slot.calendarLock!,
                result.observation,
                this.idDentist,
              )
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
      },
    );
  }

  close(): void {
    this.dialogRef.close();
  }
}
