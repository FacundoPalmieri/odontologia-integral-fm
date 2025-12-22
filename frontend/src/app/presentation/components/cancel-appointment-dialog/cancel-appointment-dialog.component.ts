import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatDialogModule, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { FormsModule } from "@angular/forms";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { AppointmentConflictInterface } from "../../../domain/interfaces/appointment.inteface";

@Component({
  selector: "app-cancel-appointment-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    IconsModule,
  ],
  templateUrl: "./cancel-appointment-dialog.component.html",
})
export class CancelAppointmentDialog {
  data: { conflict: AppointmentConflictInterface } = inject(MAT_DIALOG_DATA);
  observation: string = "";
}
