import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { ToothFaceLocationEnum } from "../../../utils/enums/tooth-face.enum";
import { TreatmentInterfaceOld } from "../../../data/interfaces/treatment.interface";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { TreatmentTypeEnum } from "../../../utils/enums/treatment.enum";

@Component({
  selector: "app-tooth-face",
  templateUrl: "./tooth-face.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule],
})
export class ToothFaceComponent {
  @Input() faceType?: ToothFaceLocationEnum;
  @Input() treatment?: TreatmentInterfaceOld;
  private treatmentsList = TreatmentFactory.createTreatments();

  righties = [ToothFaceLocationEnum.RIGHT, ToothFaceLocationEnum.TOP];
  lefties = [ToothFaceLocationEnum.LEFT, ToothFaceLocationEnum.BOTTOM];

  constructor() {}

  getTreatmentIcons(treatment: TreatmentInterfaceOld): string[] {
    const showTreatment = this.treatmentsList.find(
      (t) => t.name === treatment.name,
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
