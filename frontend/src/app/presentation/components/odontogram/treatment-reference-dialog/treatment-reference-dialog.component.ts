import { Component, inject } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatIconModule } from "@angular/material/icon";
import { TreatmentInterface } from "../../../../domain/interfaces/treatment.interface";
import { TreatmentFactory } from "../../../../utils/factories/treatment.factory";

@Component({
  selector: "app-treatment-reference-dialog",
  templateUrl: "./treatment-reference-dialog.component.html",
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
})
export class TreatmentReferenceDialogComponent {
  dialogRef = inject(MatDialogRef<TreatmentReferenceDialogComponent>);
  treatments: TreatmentInterface[] = TreatmentFactory.createTreatments();

  constructor() {}
}
