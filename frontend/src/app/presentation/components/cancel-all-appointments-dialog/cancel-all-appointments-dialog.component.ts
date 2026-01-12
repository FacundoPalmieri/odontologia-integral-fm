import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatDialogModule, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { ReactiveFormsModule, FormControl, Validators } from "@angular/forms";
import { IconsModule } from "../../../utils/tabler-icons.module";

export interface CancelAllAppointmentsDialogData {
  date: Date;
  dentistId: number;
}

@Component({
  selector: "app-cancel-all-appointments-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    IconsModule,
  ],
  templateUrl: "./cancel-all-appointments-dialog.component.html",
})
export class CancelAllAppointmentsDialog {
  data: CancelAllAppointmentsDialogData = inject(MAT_DIALOG_DATA);
  observationControl = new FormControl("", [Validators.required]);

  getFormattedDate(): string {
    return this.data.date.toLocaleDateString("es-ES", {
      weekday: "long",
      day: "numeric",
      month: "long",
      year: "numeric",
    });
  }
}
