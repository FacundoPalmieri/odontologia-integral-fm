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
import { TreatmentInterfaceOld } from "../../../domain/interfaces/treatment.interface";
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
  @Input() treatments: TreatmentInterfaceOld[] = [];
  @Output() treatmentsChange = new EventEmitter<TreatmentInterfaceOld[]>();

  toothFaceTypeEnum = ToothFaceTypeEnum;
  TreatmentEnum = TreatmentEnum;
  private treatmentsList = TreatmentFactory.createTreatments();

  private faceToPositionMap = {
    [ToothFaceEnum.OCLUSAL]: ToothFaceTypeEnum.CENTER,
    [ToothFaceEnum.LINGUAL]: ToothFaceTypeEnum.TOP,
    [ToothFaceEnum.MESIAL]: ToothFaceTypeEnum.RIGHT,
    [ToothFaceEnum.DISTAL]: ToothFaceTypeEnum.LEFT,
    [ToothFaceEnum.PALATINO]: ToothFaceTypeEnum.BOTTOM,
  };

  cariesFaces: { [key in ToothFaceTypeEnum]?: TreatmentInterfaceOld } = {};

  generalTreatments: TreatmentInterfaceOld[] = [];

  constructor() {
    this._processTreatments();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["treatments"]) {
      this._processTreatments();
    }
  }

  private _processTreatments() {
    this.cariesFaces = {};
    this.generalTreatments = [];

    // Primero, vamos a agrupar los tratamientos por cara
    const facesTreatments: {
      [key in ToothFaceTypeEnum]?: TreatmentInterfaceOld[];
    } = {};

    if (this.treatments?.length > 0) {
      this.treatments.forEach((treatment) => {
        if (
          treatment.name === TreatmentEnum.CARIES ||
          treatment.name === TreatmentEnum.OBT_COMPOSITE
        ) {
          treatment.faces?.forEach((face) => {
            const position = this.faceToPositionMap[face as ToothFaceEnum];
            if (position) {
              if (!facesTreatments[position]) {
                facesTreatments[position] = [];
              }
              facesTreatments[position]!.push(treatment);
            }
          });
        } else {
          this.generalTreatments.push(treatment);
        }
      });

      // Ahora procesamos cada cara para ver si tiene ambos tratamientos
      Object.entries(facesTreatments).forEach(([position, treatments]) => {
        const hasCaries = treatments.some(
          (t) => t.name === TreatmentEnum.CARIES
        );
        const hasObtComposite = treatments.some(
          (t) => t.name === TreatmentEnum.OBT_COMPOSITE
        );

        if (hasCaries && hasObtComposite) {
          // Si tiene ambos, creamos un tratamiento especial con la letra D
          this.cariesFaces[position as ToothFaceTypeEnum] = {
            name: TreatmentEnum.DUAL_TREATMENT,
            treatmentType: TreatmentTypeEnum.DONE,
            label: "D",
            faces: [],
          };
        } else if (treatments.length > 0) {
          // Si solo tiene uno, usamos el primer tratamiento
          this.cariesFaces[position as ToothFaceTypeEnum] = treatments[0];
        }
      });
    }
  }

  getTreatmentIcons(treatment: TreatmentInterfaceOld): string[] {
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
      .subscribe((treatments: TreatmentInterfaceOld[]) => {
        if (treatments) {
          this.treatmentsChange.emit(treatments);
        }
      });
  }
}
