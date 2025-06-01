import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { ToothFaceTypeEnum } from "../../../utils/enums/tooth-face-type.enum";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { TreatmentTypeEnum } from "../../../utils/enums/treatment.enum";

@Component({
  selector: "app-tooth-face",
  templateUrl: "./tooth-face.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule],
  styles: [
    `
      .clip-left {
        clip-path: polygon(0 0, 50% 0, 50% 100%, 0 100%);
      }
    `,
  ],
})
export class ToothFaceComponent {
  @Input() faceType?: ToothFaceTypeEnum;
  @Input() treatment?: TreatmentInterface;
  private treatmentsList = TreatmentFactory.createTreatments();

  righties = [ToothFaceTypeEnum.RIGHT, ToothFaceTypeEnum.TOP];
  lefties = [ToothFaceTypeEnum.LEFT, ToothFaceTypeEnum.BOTTOM];

  constructor() {}

  getTreatmentIcons(treatment: TreatmentInterface): string[] {
    const showTreatment = this.treatmentsList.find(
      (t) => t.name === treatment.name
    );
    return showTreatment?.icons || [];
  }

  calculateMargin(index: number, totalIcons: number): string {
    if (totalIcons > 1 && index < totalIcons - 1) {
      return "mr-[-18px]";
    }
    return "";
  }

  getColor(treatmentType: TreatmentTypeEnum): string {
    return treatmentType === TreatmentTypeEnum.REQUIRED
      ? "text-blue-500"
      : "text-red-500";
  }
}
