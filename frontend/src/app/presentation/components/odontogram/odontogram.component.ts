import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";
import { ToothComponent } from "../tooth/tooth.component";
import { MatDividerModule } from "@angular/material/divider";

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
  upperTeethLeft = [18, 17, 16, 15, 14, 13, 12, 11];
  upperTeethRight = [21, 22, 23, 24, 25, 26, 27, 28];
  lowerTeethLeft = [48, 47, 46, 45, 44, 43, 42, 41];
  lowerTeethRight = [31, 32, 33, 34, 35, 36, 37, 38];

  temporaryUpperLeft = [55, 54, 53, 52, 51];
  temporaryUpperRight = [61, 62, 63, 64, 65];
  temporaryLowerLeft = [85, 84, 83, 82, 81];
  temporaryLowerRight = [71, 72, 73, 74, 75];
  constructor() {}
}
