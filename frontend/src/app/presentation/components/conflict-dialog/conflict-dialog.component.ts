import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import {
  MatDialogModule,
  MAT_DIALOG_DATA,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatTableModule } from "@angular/material/table";
import { MatTooltipModule } from "@angular/material/tooltip";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { AppointmentConflictInterface } from "../../../domain/interfaces/appointment.inteface";

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
    IconsModule,
  ],
  templateUrl: "./conflict-dialog.component.html",
})
export class ConflictDialogComponent {
  data: { conflicts: AppointmentConflictInterface[] };
  conflicts: AppointmentConflictInterface[];
  displayedColumns = ["patientName", "date", "origin"];

  constructor(public dialogRef: MatDialogRef<ConflictDialogComponent>) {
    this.data = inject(MAT_DIALOG_DATA);
    this.conflicts = this.data.conflicts;
    console.log("Conflicts received:", this.conflicts);
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
}
