import {
  Component,
  inject,
  Input,
  Output,
  EventEmitter,
  SimpleChanges,
  OnChanges,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatMenuModule } from "@angular/material/menu";
import { MatListModule } from "@angular/material/list";
import { TreatmentInterface } from "../../../domain/interfaces/treatment.interface";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { ToothFaceTypeEnum } from "../../../utils/enums/tooth-face-type.enum";
import { AddTreatmentDialogComponent } from "../add-treatment-dialog/add-treatment-dialog.component";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import {
  TreatmentEnum,
  TreatmentTypeEnum,
} from "../../../utils/enums/treatment.enum";
import { ToothFaceEnum } from "../../../utils/factories/tooth-face.factory";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { ToothFaceComponent } from "../tooth-face/tooth-face.component";

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
    ToothFaceComponent,
    MatDialogModule,
  ],
})
export class ToothComponent implements OnChanges {
  private readonly dialog = inject(MatDialog);
  @Input() toothNumber: number = 0;
  @Input() treatments: TreatmentInterface[] = [];
  @Output() treatmentsChange = new EventEmitter<TreatmentInterface[]>();

  toothFaceTypeEnum = ToothFaceTypeEnum;
  TreatmentEnum = TreatmentEnum;
  private treatmentsList = TreatmentFactory.createTreatments();

  // Mapeo de caras del diente a sus posiciones
  private faceToPositionMap = {
    [ToothFaceEnum.INCISAL]: ToothFaceTypeEnum.TOP,
    [ToothFaceEnum.LINGUAL]: ToothFaceTypeEnum.BOTTOM,
    [ToothFaceEnum.MESIAL]: ToothFaceTypeEnum.LEFT,
    [ToothFaceEnum.DISTAL]: ToothFaceTypeEnum.RIGHT,
    [ToothFaceEnum.VESTIBULAR]: ToothFaceTypeEnum.CENTER,
  };

  // Tratamientos por cara (para caries y composite)
  cariesFaces: { [key in ToothFaceTypeEnum]?: TreatmentInterface } = {};

  // Tratamientos generales
  generalTreatments: TreatmentInterface[] = [];

  constructor() {
    this._processTreatments();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["treatments"]) {
      this._processTreatments();
    }
  }

  private _processTreatments() {
    // Resetear todos los tratamientos
    this.cariesFaces = {};
    this.generalTreatments = [];

    if (this.treatments?.length > 0) {
      this.treatments.forEach((treatment) => {
        if (
          treatment.name === TreatmentEnum.CARIES ||
          treatment.name === TreatmentEnum.OBT_COMPOSITE
        ) {
          // Procesar caras con caries u obturación
          treatment.faces?.forEach((face) => {
            const position = this.faceToPositionMap[face as ToothFaceEnum];
            if (position) {
              this.cariesFaces[position] = treatment;
            }
          });
        } else {
          this.generalTreatments.push(treatment);
        }
      });
    }
  }

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

  openAddTreatmentDialog() {
    this.dialog
      .open(AddTreatmentDialogComponent, {
        width: "800px",
        data: {
          toothNumber: this.toothNumber,
          treatments: this.treatments,
        },
      })
      .afterClosed()
      .subscribe((treatments: TreatmentInterface[]) => {
        if (treatments) {
          this.treatmentsChange.emit(treatments);
        }
      });
  }
}
