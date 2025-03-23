import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";
import { ToothComponent } from "../tooth/tooth.component";
import { MatDividerModule } from "@angular/material/divider";
import { OdontogramInterface } from "../../../domain/interfaces/odontogram.interface";
import { mockOdontogram } from "../../../utils/mocks/odontogram.mock";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { ToothInterface } from "../../../domain/interfaces/tooth.interface";

@Component({
  selector: "app-odontogram",
  templateUrl: "./odontogram.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatMenuModule,
    ToothComponent,
    MatDividerModule,
  ],
})
export class OdontogramComponent {
  odontogram: OdontogramInterface = mockOdontogram;

  constructor() {}

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
      const foundTooth = teethArray.find(
        (tooth: ToothInterface) => tooth.number === toothNumber
      );
      if (foundTooth) {
        return foundTooth;
      }
    }
    return undefined;
  }
}
