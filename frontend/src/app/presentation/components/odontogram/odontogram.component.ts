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
import {
  TreatmentEnum,
  TreatmentTypeEnum,
} from "../../../utils/enums/treatment.enum";
import { MatSelectModule } from "@angular/material/select";
import { FormsModule } from "@angular/forms";
import {
  mockOdontogram1,
  mockOdontogram2,
  mockOdontogram3,
} from "../../../utils/mocks/odontogram.mock";

interface BridgeConnectionInterface {
  startTooth: number;
  endTooth: number;
  treatment: TreatmentInterface;
}

interface OdontogramDateInterface {
  date: Date;
  odontogram: OdontogramInterface;
}

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
    MatSelectModule,
    FormsModule,
  ],
})
export class OdontogramComponent implements OnChanges {
  mockOdontogram1 = mockOdontogram1;
  mockOdontogram2 = mockOdontogram2;
  mockOdontogram3 = mockOdontogram3;
  baseOdontogram: OdontogramInterface = {
    upperTeethLeft: [
      { number: 18 },
      { number: 17 },
      { number: 16 },
      { number: 15 },
      { number: 14 },
      { number: 13 },
      { number: 12 },
      { number: 11 },
    ],
    upperTeethRight: [
      { number: 21 },
      { number: 22 },
      { number: 23 },
      { number: 24 },
      { number: 25 },
      { number: 26 },
      { number: 27 },
      { number: 28 },
    ],
    lowerTeethLeft: [
      { number: 48 },
      { number: 47 },
      { number: 46 },
      { number: 45 },
      { number: 44 },
      { number: 43 },
      { number: 42 },
      { number: 41 },
    ],
    lowerTeethRight: [
      { number: 31 },
      { number: 32 },
      { number: 33 },
      { number: 34 },
      { number: 35 },
      { number: 36 },
      { number: 37 },
      { number: 38 },
    ],
  };

  odontogramDates: OdontogramDateInterface[] = [
    {
      date: new Date(2025, 2, 15),
      odontogram: this.mockOdontogram1,
    },
    {
      date: new Date(2024, 11, 20),
      odontogram: this.mockOdontogram2,
    },
    {
      date: new Date(2024, 8, 5),
      odontogram: this.mockOdontogram3,
    },
  ];

  @Input() title: string = "Odontograma";
  @Input() odontogram: OdontogramInterface = this.baseOdontogram;
  @Input() showTemporaries?: boolean = false;
  @Input() showToolbox: boolean = false;
  @Input() showDateSelector: boolean = false;
  treatmentTypeEnum = TreatmentTypeEnum;

  treatmentReferencesSidenavService = inject(TreatmentReferencesSidenavService);

  bridgeConnections: BridgeConnectionInterface[] = [];
  selectedDate: OdontogramDateInterface = this.odontogramDates[0];
  selectedOdontogram: OdontogramInterface = this.selectedDate.odontogram;

  constructor() {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["odontogram"]) {
      this.detectBridges();
    }
  }

  private detectBridges() {
    this.bridgeConnections = [];
    const allTeeth = [
      ...(this.odontogram.upperTeethLeft || []),
      ...(this.odontogram.upperTeethRight || []),
      ...(this.odontogram.lowerTeethLeft || []),
      ...(this.odontogram.lowerTeethRight || []),
      ...(this.odontogram.temporaryUpperLeft || []),
      ...(this.odontogram.temporaryUpperRight || []),
      ...(this.odontogram.temporaryLowerLeft || []),
      ...(this.odontogram.temporaryLowerRight || []),
    ];

    allTeeth.forEach((tooth) => {
      if (tooth.treatments) {
        tooth.treatments.forEach((treatment: TreatmentInterface) => {
          if (
            treatment.name === TreatmentEnum.PUENTE &&
            treatment.bridgeStart &&
            treatment.bridgeEnd
          ) {
            this.bridgeConnections.push({
              startTooth: treatment.bridgeStart,
              endTooth: treatment.bridgeEnd,
              treatment,
            });
          }
        });
      }
    });
  }

  isUpperTooth(toothNumber: number): boolean {
    const upperTeeth = [
      ...(this.odontogram.upperTeethLeft || []),
      ...(this.odontogram.upperTeethRight || []),
      ...(this.odontogram.temporaryUpperLeft || []),
      ...(this.odontogram.temporaryUpperRight || []),
    ];
    return upperTeeth.some((t) => t.number === toothNumber);
  }

  getToothPosition(toothNumber: number): { x: number; y: number } {
    // Constants for tooth positioning
    const TOOTH_WIDTH = 72; // Width of each tooth component
    const TOOTH_HEIGHT = 72; // Height of each tooth component
    const SECTION_GAP = 16; // Gap between sections
    const UPPER_LOWER_GAP = 16; // Gap between upper and lower sections

    let section:
      | "upperLeft"
      | "upperRight"
      | "lowerLeft"
      | "lowerRight"
      | "tempUpperLeft"
      | "tempUpperRight"
      | "tempLowerLeft"
      | "tempLowerRight";
    let index = -1;

    if (
      this.odontogram.upperTeethLeft?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "upperLeft";
    } else if (
      this.odontogram.upperTeethRight?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "upperRight";
    } else if (
      this.odontogram.lowerTeethLeft?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "lowerLeft";
    } else if (
      this.odontogram.lowerTeethRight?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "lowerRight";
    } else if (
      this.odontogram.temporaryUpperLeft?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "tempUpperLeft";
    } else if (
      this.odontogram.temporaryUpperRight?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "tempUpperRight";
    } else if (
      this.odontogram.temporaryLowerLeft?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "tempLowerLeft";
    } else if (
      this.odontogram.temporaryLowerRight?.find((t, i) => {
        index = i;
        return t.number === toothNumber;
      })
    ) {
      section = "tempLowerRight";
    } else {
      return { x: 0, y: 0 };
    }

    let x = 0;
    let y = 0;

    if (section.includes("Left")) {
      x = index * TOOTH_WIDTH;
    } else if (section.includes("Right")) {
      x = (index + 8) * TOOTH_WIDTH + SECTION_GAP;
    }

    if (section.includes("upper")) {
      y = 0;
    } else if (section.includes("lower")) {
      y = TOOTH_HEIGHT + UPPER_LOWER_GAP;
    }

    if (section.includes("temp")) {
      y += (TOOTH_HEIGHT + UPPER_LOWER_GAP) * 2;
    }

    x += TOOTH_WIDTH / 2;
    y += TOOTH_HEIGHT / 2;

    return { x, y };
  }

  onTreatmentsChange(toothNumber: number, treatments: TreatmentInterface[]) {
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

    let found = false;
    for (const section of sections) {
      const tooth = section?.find((t) => t.number === toothNumber);
      if (tooth) {
        tooth.treatments = [...treatments];
        found = true;
        break;
      }
    }

    if (found) {
      this.odontogram = {
        ...this.odontogram,
        upperTeethLeft: [...(this.odontogram.upperTeethLeft || [])],
        upperTeethRight: [...(this.odontogram.upperTeethRight || [])],
        lowerTeethLeft: [...(this.odontogram.lowerTeethLeft || [])],
        lowerTeethRight: [...(this.odontogram.lowerTeethRight || [])],
        temporaryUpperLeft: [...(this.odontogram.temporaryUpperLeft || [])],
        temporaryUpperRight: [...(this.odontogram.temporaryUpperRight || [])],
        temporaryLowerLeft: [...(this.odontogram.temporaryLowerLeft || [])],
        temporaryLowerRight: [...(this.odontogram.temporaryLowerRight || [])],
      };
      this.detectBridges();
    }
  }

  onDateChange(date: OdontogramDateInterface) {
    this.selectedDate = date;
    this.odontogram = date.odontogram;
    this.detectBridges();
  }
}
