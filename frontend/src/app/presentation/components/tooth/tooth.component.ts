import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";
import { MatListModule } from "@angular/material/list";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-tooth",
  templateUrl: "./tooth.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatMenuModule,
    MatButtonModule,
    MatTooltipModule,
    MatListModule,
  ],
})
export class ToothComponent {
  @Input() toothNumber: number = 0;

  topIcons: string[] = ["rectangle-rounded-top"];
  bottomIcons: string[] = ["rectangle-rounded-bottom"];
  leftIcons: string[] = ["rectangle-rounded-bottom"];
  rightIcons: string[] = ["rectangle-rounded-bottom"];
  centerIcons: string[] = ["square"];
  treatments: TreatmentInterface[] = TreatmentFactory.createTreatments();

  constructor() {}

  updateTopIcon(treatment: TreatmentInterface) {
    this.topIcons = treatment.icons!;
  }

  updateBottomIcon(treatment: TreatmentInterface) {
    this.bottomIcons = treatment.icons!;
  }

  updateLeftIcon(treatment: TreatmentInterface) {
    this.leftIcons = treatment.icons!;
  }

  updateRightIcon(treatment: TreatmentInterface) {
    this.rightIcons = treatment.icons!;
  }

  updateCenterIcon(treatment: TreatmentInterface) {
    this.centerIcons = treatment.icons!;
  }

  calculateMargin(index: number, totalIcons: number): string {
    if (totalIcons > 1 && index < totalIcons - 1) {
      return "mr-[-16px]";
    }
    return "";
  }
}
