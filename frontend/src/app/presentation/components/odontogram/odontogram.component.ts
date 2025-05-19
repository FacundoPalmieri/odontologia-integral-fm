import {
  Component,
  inject,
  Input,
  OnChanges,
  SimpleChanges,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";
import { ToothComponent } from "../tooth/tooth.component";
import { MatDividerModule } from "@angular/material/divider";
import { OdontogramInterface } from "../../../domain/interfaces/odontogram.interface";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSidenavModule } from "@angular/material/sidenav";
import { TreatmentReferencesSidenavService } from "../../../services/treatment-references-sidenav.service";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";

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
export class OdontogramComponent {
  baseOdontogram: OdontogramInterface = {
    upperTeethLeft: [
      {
        number: 18,
      },
      {
        number: 17,
      },
      {
        number: 16,
      },
      {
        number: 15,
      },
      {
        number: 14,
      },
      {
        number: 13,
      },
      {
        number: 12,
      },
      {
        number: 11,
      },
    ],
    upperTeethRight: [
      {
        number: 21,
      },
      {
        number: 22,
      },
      {
        number: 23,
      },
      {
        number: 24,
      },
      {
        number: 25,
      },
      {
        number: 26,
      },
      {
        number: 27,
      },
      {
        number: 28,
      },
    ],
    lowerTeethLeft: [
      {
        number: 48,
      },
      {
        number: 47,
      },
      {
        number: 46,
      },
      {
        number: 45,
      },
      {
        number: 44,
      },
      {
        number: 43,
      },
      {
        number: 42,
      },
      {
        number: 41,
      },
    ],
    lowerTeethRight: [
      {
        number: 31,
      },
      {
        number: 32,
      },
      {
        number: 33,
      },
      {
        number: 34,
      },
      {
        number: 35,
      },
      {
        number: 36,
      },
      {
        number: 37,
      },
      {
        number: 38,
      },
    ],
    temporaryUpperLeft: [
      {
        number: 55,
      },
      {
        number: 54,
      },
      {
        number: 53,
      },
      {
        number: 52,
      },
      {
        number: 51,
      },
    ],
    temporaryUpperRight: [
      {
        number: 61,
      },
      {
        number: 62,
      },
      {
        number: 63,
      },
      {
        number: 64,
      },
      {
        number: 65,
      },
    ],
    temporaryLowerLeft: [
      {
        number: 85,
      },
      {
        number: 84,
      },
      {
        number: 83,
      },
      {
        number: 82,
      },
      {
        number: 81,
      },
    ],
    temporaryLowerRight: [
      {
        number: 71,
      },
      {
        number: 72,
      },
      {
        number: 73,
      },
      {
        number: 74,
      },
      {
        number: 75,
      },
    ],
  };
  @Input() title: string = "Odontograma";
  @Input() odontogram: OdontogramInterface = this.baseOdontogram;
  @Input() showTemporaries?: boolean = false;
  @Input() showToolbox: boolean = false;

  treatmentReferencesSidenavService = inject(TreatmentReferencesSidenavService);

  constructor() {}

  onTreatmentsChange(toothNumber: number, treatments: TreatmentInterface[]) {
    console.log("treatments", treatments);
    const sections = [
      this.odontogram.upperTeethLeft,
      this.odontogram.upperTeethRight,
      this.odontogram.lowerTeethLeft,
      this.odontogram.lowerTeethRight,
      this.odontogram.temporaryUpperLeft,
      this.odontogram.temporaryUpperRight,
      this.odontogram.temporaryLowerLeft,
      this.odontogram.temporaryLowerRight,
    ];

    for (const section of sections) {
      const tooth = section?.find((t) => t.number === toothNumber);
      if (tooth) {
        tooth.treatments = [...treatments];
        this.odontogram = { ...this.odontogram };
        break;
      }
    }
  }
}
