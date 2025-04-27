import { Component, inject, Input, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";
import { ToothComponent } from "../tooth/tooth.component";
import { MatDividerModule } from "@angular/material/divider";
import { OdontogramInterface } from "../../../domain/interfaces/odontogram.interface";
import { mockOdontogram } from "../../../utils/mocks/odontogram.mock";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { ToothInterface } from "../../../domain/interfaces/tooth.interface";
import { MatDialog } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSidenavModule } from "@angular/material/sidenav";
import { TreatmentReferencesSidenavService } from "../../../services/treatment-references-sidenav.service";
import { ConfirmDialogComponent } from "../confirm-dialog/confirm-dialog.component";

@Component({
  selector: "app-odontogram",
  templateUrl: "./odontogram.component.html",
  styleUrl: "./odontogram.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatMenuModule,
    ToothComponent,
    MatDividerModule,
    MatButtonModule,
    MatTooltipModule,
    MatSidenavModule,
  ],
})
export class OdontogramComponent implements OnInit {
  @Input() inputOdontogram?: OdontogramInterface;
  @Input() showTemporaries?: boolean = false;

  dialog = inject(MatDialog);
  treatmentReferencesSidenavService = inject(TreatmentReferencesSidenavService);
  odontogram!: OdontogramInterface;

  constructor() {}

  ngOnInit(): void {
    this.odontogram = this.inputOdontogram || mockOdontogram;
  }

  onFullToothTreatmentChange(
    toothNumber: number,
    treatment: TreatmentInterface
  ) {
    const tooth = this.findTooth(toothNumber);
    if (tooth) {
      tooth.topTreatment = treatment;
      tooth.bottomTreatment = treatment;
      tooth.leftTreatment = treatment;
      tooth.rightTreatment = treatment;
      tooth.centerTreatment = treatment;
    }
  }

  onTopTreatmentChange(toothNumber: number, treatment: TreatmentInterface) {
    this.updateToothTreatment(toothNumber, "topTreatment", treatment);
  }

  onBottomTreatmentChange(toothNumber: number, treatment: TreatmentInterface) {
    this.updateToothTreatment(toothNumber, "bottomTreatment", treatment);
  }

  onLeftTreatmentChange(toothNumber: number, treatment: TreatmentInterface) {
    this.updateToothTreatment(toothNumber, "leftTreatment", treatment);
  }

  onRightTreatmentChange(toothNumber: number, treatment: TreatmentInterface) {
    this.updateToothTreatment(toothNumber, "rightTreatment", treatment);
  }

  onCenterTreatmentChange(toothNumber: number, treatment: TreatmentInterface) {
    this.updateToothTreatment(toothNumber, "centerTreatment", treatment);
  }

  updateToothTreatment(
    toothNumber: number,
    treatmentType: keyof Omit<ToothInterface, "number">,
    treatment: TreatmentInterface
  ) {
    const tooth = this.findTooth(toothNumber);
    if (tooth) {
      tooth[treatmentType] = treatment;
    }
  }

  findTooth(toothNumber: number): ToothInterface | undefined {
    for (const teethArray of [
      this.odontogram.upperTeethLeft,
      this.odontogram.upperTeethRight,
      this.odontogram.lowerTeethLeft,
      this.odontogram.lowerTeethRight,
      this.odontogram.temporaryUpperLeft,
      this.odontogram.temporaryUpperRight,
      this.odontogram.temporaryLowerLeft,
      this.odontogram.temporaryLowerRight,
    ]) {
      const foundTooth = teethArray?.find(
        (tooth: ToothInterface) => tooth.number === toothNumber
      );
      if (foundTooth) {
        return foundTooth;
      }
    }
    return undefined;
  }

  clearOdontogram(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "400px",
      data: { message: "¿Estás seguro de que quieres limpiar el odontograma?" },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const resetTeeth = (teeth: ToothInterface[]) => {
          teeth.forEach((tooth) => {
            tooth.topTreatment = undefined;
            tooth.bottomTreatment = undefined;
            tooth.leftTreatment = undefined;
            tooth.rightTreatment = undefined;
            tooth.centerTreatment = undefined;
          });
        };

        resetTeeth(this.odontogram.upperTeethLeft);
        resetTeeth(this.odontogram.upperTeethRight);
        resetTeeth(this.odontogram.lowerTeethLeft);
        resetTeeth(this.odontogram.lowerTeethRight);
        resetTeeth(this.odontogram.temporaryUpperLeft!);
        resetTeeth(this.odontogram.temporaryUpperRight!);
        resetTeeth(this.odontogram.temporaryLowerLeft!);
        resetTeeth(this.odontogram.temporaryLowerRight!);
      }
    });
  }
}
