import { Component, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { ToothFaceTypeEnum } from "../../../utils/enums/tooth-face-type.enum";

@Component({
  selector: "app-tooth-face",
  templateUrl: "./tooth-face.component.html",
  standalone: true,
  imports: [CommonModule, IconsModule],
})
export class ToothFaceComponent {
  @Input() faceType?: ToothFaceTypeEnum;
  @Input() treatment?: TreatmentInterface;

  righties = [ToothFaceTypeEnum.RIGHT, ToothFaceTypeEnum.TOP];
  lefties = [ToothFaceTypeEnum.LEFT, ToothFaceTypeEnum.BOTTOM];

  constructor() {}

  calculateMargin(index: number, totalIcons: number): string {
    if (totalIcons > 1 && index < totalIcons - 1) {
      return "mr-[-18px]";
    }
    return "";
  }
}
