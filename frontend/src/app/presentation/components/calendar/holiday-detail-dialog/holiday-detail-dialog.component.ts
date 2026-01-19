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
  private dialog = inject(MatDialog);

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { holiday: HolidayInterface; date: Date },
    private dialogRef: MatDialogRef<HolidayDetailDialogComponent>,
  ) {
    this.holiday = data.holiday;
    this.date = data.date;
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
}
