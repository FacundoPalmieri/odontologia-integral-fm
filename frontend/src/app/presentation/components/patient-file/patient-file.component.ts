import { Component, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MAT_DIALOG_DATA, MatDialogModule } from "@angular/material/dialog";
import { PatientInterface } from "../../../domain/interfaces/patient.interface";
import { MatStepperModule } from "@angular/material/stepper";
import { MatButtonModule } from "@angular/material/button";
import { OdontogramComponent } from "../odontogram/odontogram.component";
import { mockOdontogram } from "../../../utils/mocks/odontogram.mock";
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: "app-patient-file",
  templateUrl: "./patient-file.component.html",
  styleUrl: "./patient-file.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatStepperModule,
    MatButtonModule,
    OdontogramComponent,
    MatIconModule,
  ],
})
export class PatientFileComponent {
  data: PatientInterface;
  odontogram = mockOdontogram;

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
  }
}
