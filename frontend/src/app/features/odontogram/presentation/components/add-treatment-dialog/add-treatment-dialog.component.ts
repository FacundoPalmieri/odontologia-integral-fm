import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatRadioModule } from "@angular/material/radio";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatTableModule } from "@angular/material/table";
import { MatTooltipModule } from "@angular/material/tooltip";
import {
  TreatmentInterfaceOld,
  ShowTreatmentInterface,
} from "../../../data/interfaces/treatment.interface";
import { ToothFaceFactory } from "../../../utils/factories/tooth-face.factory";
import {
  TreatmentEnum,
  TreatmentConditionEnum,
} from "../../../utils/enums/treatment.enum";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { ToothFaceInterface } from "../../../data/interfaces/tooth.interface";
import { TreatmentService } from "../../../services/treatment.service";
import { TreatmentConditionDto, TreatmentDto } from "../../../data/dtos/treatment.dto";

interface AddTreatmentDialogData {
  toothNumber: number;
  treatments: TreatmentInterfaceOld[];
}

@Component({
  selector: "app-add-treatment-dialog",
  templateUrl: "./add-treatment-dialog.component.html",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatCheckboxModule,
    IconsModule,
    MatTableModule,
    MatTooltipModule,
  ],
})
export class AddTreatmentDialogComponent implements OnInit {
  dialogRef = inject(MatDialogRef<AddTreatmentDialogComponent>);
  snackbarService = inject(SnackbarService);
  treatmentService = inject(TreatmentService);
  treatmentForm: FormGroup = new FormGroup({});
  data: AddTreatmentDialogData = inject(MAT_DIALOG_DATA);

  treatmentsCatalog = signal<TreatmentDto[]>([]);
  selectedTreatment = signal<TreatmentDto | null>(null);
  availableConditions = signal<TreatmentConditionDto[]>([]);
  selectedConditionId = signal<number | null>(null);
  selectedCondition = computed(() => {
    const id = this.selectedConditionId();
    if (!id) return null;
    return this.availableConditions().find((c) => c.id === id) ?? null;
  });

  TreatmentEnum = TreatmentEnum;
  TreatmentTypeEnum = TreatmentConditionEnum;

  treatmentsList = signal<TreatmentInterfaceOld[]>([]);
  displayedColumns: string[] = [
    "treatment",
    "observations",
    "treatmentType",
    "actions",
  ];

  toothFaces: ToothFaceInterface[] = [];

  constructor() {
    this.toothFaces = ToothFaceFactory.createToothFaces(this.data.toothNumber);
    this._loadForm();
    if (this.data.treatments?.length > 0) {
      this.treatmentsList.set([...this.data.treatments]);
    }
  }

  ngOnInit() {
    this.treatmentService.getAll().subscribe({
      next: (response) => {
        if (response.data) {
          this.treatmentsCatalog.set(response.data.content);
        }
      },
      error: (err) => {
        console.error("Error loading treatments:", err);
      },
    });

    this.treatmentForm
      .get("treatment")
      ?.valueChanges.subscribe((treatmentName) => {
        const found = this.treatmentsCatalog().find((t) => t.name === treatmentName) ?? null;
        this.selectedTreatment.set(found);
        this.availableConditions.set(found?.conditions ?? []);

        const typeCtrl = this.treatmentForm.get("treatmentType");
        typeCtrl?.setValue(null);
        if (found && this.availableConditions().length > 0) {
          typeCtrl?.enable();
        } else {
          typeCtrl?.disable();
        }

        this._cleanupCariesControls();
        this.treatmentForm.removeControl("bridgeStart");
        this.treatmentForm.removeControl("bridgeEnd");

        if (found) {
          if (found.name === TreatmentEnum.PUENTE) {
            this.treatmentForm.addControl(
              "bridgeStart",
              new FormControl(this.data.toothNumber),
            );
            this.treatmentForm.addControl("bridgeEnd", new FormControl(""));
          } else if (
            found.name === TreatmentEnum.CARIES ||
            found.name === TreatmentEnum.OBTURACION_COMPOSITE
          ) {
            this._initializeCariesControls();
          }
        }
      });

    this.treatmentForm
      .get("treatmentType")
      ?.valueChanges.subscribe((typeValue) => {
        this.selectedConditionId.set(typeValue);
      });
  }

  onFaceChange(faceId: string, checked: boolean) {
    if (!checked && this.treatmentForm.get("allFaces")?.value) {
      this.treatmentForm.get("allFaces")?.setValue(false, { emitEvent: false });
    }

    const allSelected = this.toothFaces.every(
      (face) => this.treatmentForm.get(`face_${face.face}`)?.value,
    );

    if (allSelected) {
      this.treatmentForm.get("allFaces")?.setValue(true, { emitEvent: false });
    }
  }

  private _handleAllFacesChange(checked: boolean) {
    this.toothFaces.forEach((face) => {
      this.treatmentForm
        .get(`face_${face.face}`)
        ?.setValue(checked, { emitEvent: false });
    });
  }

  private isUpperTooth(toothNumber: number): boolean {
    // Dientes superiores: 11-18, 21-28, 51-55, 61-65
    return (
      (toothNumber >= 11 && toothNumber <= 18) ||
      (toothNumber >= 21 && toothNumber <= 28) ||
      (toothNumber >= 51 && toothNumber <= 55) ||
      (toothNumber >= 61 && toothNumber <= 65)
    );
  }

  private validateBridgeTeeth(startTooth: number, endTooth: number): boolean {
    const startIsUpper = this.isUpperTooth(startTooth);
    const endIsUpper = this.isUpperTooth(endTooth);

    if (endTooth < 11 || endTooth > 85) {
      this.snackbarService.openSnackbar(
        "El número de diente debe estar entre 11 y 85",
        6000,
        "center",
        "top",
        SnackbarTypeEnum.Info,
      );
      return false;
    }

    if (startIsUpper !== endIsUpper) {
      this.snackbarService.openSnackbar(
        "Los puentes solo pueden conectarse entre dientes de la misma línea (superior o inferior)",
        6000,
        "center",
        "top",
        SnackbarTypeEnum.Info,
      );
      return false;
    }

    if (endTooth <= 0) {
      this.snackbarService.openSnackbar(
        "Debe seleccionar un diente válido",
        6000,
        "center",
        "top",
        SnackbarTypeEnum.Info,
      );
      return false;
    }

    if (startTooth === endTooth) {
      this.snackbarService.openSnackbar(
        "El puente debe conectarse a un diente diferente",
        6000,
        "center",
        "top",
        SnackbarTypeEnum.Info,
      );
      return false;
    }

    return true;
  }

  addTreatment() {
    const formValue = this.treatmentForm.value;
    if (!formValue.treatment || !formValue.treatmentType) {
      this.snackbarService.openSnackbar(
        "Seleccioná un tratamiento y una condición del catálogo.",
        4000,
        "center",
        "top",
        SnackbarTypeEnum.Info,
      );
      return;
    }

    const condition = this.availableConditions().find((c) => c.id === formValue.treatmentType);
    const treatment: TreatmentInterfaceOld = {
      name: formValue.treatment,
      label: this.selectedTreatment()?.label ?? "Tratamiento",
      treatmentType: formValue.treatmentType,
      treatmentConditionName: condition?.name,
      treatmentConditionColor: condition?.color,
    };

    if (formValue.treatment === TreatmentEnum.PUENTE) {
      if (
        !this.validateBridgeTeeth(formValue.bridgeStart, formValue.bridgeEnd)
      ) {
        return;
      }
      treatment.bridgeStart = formValue.bridgeStart;
      treatment.bridgeEnd = formValue.bridgeEnd;
    } else if (
      formValue.treatment === TreatmentEnum.CARIES ||
      formValue.treatment === TreatmentEnum.OBTURACION_COMPOSITE
    ) {
      const selectedFaces = this.toothFaces
        .filter((face) => formValue[`face_${face.face}`])
        .map((face) => face.face);

      if (selectedFaces.length === 0) {
        this.snackbarService.openSnackbar(
          "Seleccioná al menos una cara para este tratamiento.",
          4000,
          "center",
          "top",
          SnackbarTypeEnum.Info,
        );
        return;
      }
      treatment.faces = selectedFaces;
    }

    this.treatmentsList.update((list) => [...list, treatment]);

    this._cleanupCariesControls();
    this.treatmentForm.reset({
      treatment: null,
      treatmentType: null,
    });
    this.selectedTreatment.set(null);
    this.availableConditions.set([]);
  }

  removeTreatment(index: number) {
    this.treatmentsList.update((list) => {
      const newList = [...list];
      newList.splice(index, 1);
      return newList;
    });
  }

  getConditionColor(element: TreatmentInterfaceOld): string {
    if (element.treatmentConditionColor) return element.treatmentConditionColor;
    return element.treatmentType === TreatmentConditionEnum.REQUIRED ? "#3b82f6" : "#ef4444";
  }

  getConditionName(element: TreatmentInterfaceOld): string {
    if (element.treatmentConditionName) return element.treatmentConditionName;
    return element.treatmentType === TreatmentConditionEnum.REQUIRED ? "Requerida" : "Existente";
  }

  private _loadForm() {
    this.treatmentForm = new FormGroup({
      treatment: new FormControl<string | null>(null),
      treatmentType: new FormControl<number | null>({ value: null, disabled: true }),
    });
  }

  private _initializeCariesControls() {
    this.treatmentForm.addControl("allFaces", new FormControl(false));
    this.toothFaces.forEach((face) => {
      this.treatmentForm.addControl(
        `face_${face.face}`,
        new FormControl(false),
      );
    });

    this.treatmentForm.get("allFaces")?.valueChanges.subscribe((checked) => {
      this._handleAllFacesChange(checked);
    });
  }

  private _cleanupCariesControls() {
    this.treatmentForm.removeControl("allFaces");
    this.toothFaces.forEach((face) => {
      this.treatmentForm.removeControl(`face_${face.face}`);
    });
  }
}
