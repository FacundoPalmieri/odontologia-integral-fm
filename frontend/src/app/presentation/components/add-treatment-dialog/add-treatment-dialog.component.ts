import { Component, inject, OnInit } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatRadioModule } from "@angular/material/radio";
import { TreatmentFactory } from "../../../utils/factories/treatment.factory";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatTableModule } from "@angular/material/table";
import { MatTooltipModule } from "@angular/material/tooltip";
import {
  TreatmentInterfaceOld,
  ShowTreatmentInterface,
} from "../../../domain/interfaces/treatment.interface";
import { ToothFaceInterface } from "../../../domain/interfaces/tooth.interface";
import { ToothFaceFactory } from "../../../utils/factories/tooth-face.factory";
import {
  TreatmentEnum,
  TreatmentTypeEnum,
} from "../../../utils/enums/treatment.enum";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";

interface AddTreatmentDialogData {
  toothNumber: number;
  treatments: TreatmentInterfaceOld[];
}

@Component({
  selector: "app-add-treatment-dialog",
  templateUrl: "./add-treatment-dialog.component.html",
  standalone: true,
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
    MatSnackBarModule,
  ],
})
export class AddTreatmentDialogComponent implements OnInit {
  dialogRef = inject(MatDialogRef<AddTreatmentDialogComponent>);
  snackbarService = inject(SnackbarService);
  treatmentForm: FormGroup = new FormGroup({});
  data: AddTreatmentDialogData = inject(MAT_DIALOG_DATA);

  treatments: ShowTreatmentInterface[] = TreatmentFactory.createTreatments();
  selectedTreatment = this.treatments[0];
  TreatmentEnum = TreatmentEnum;
  TreatmentTypeEnum = TreatmentTypeEnum;

  treatmentsList: TreatmentInterfaceOld[] = [];
  displayedColumns: string[] = [
    "treatment",
    "observations",
    "treatmentType",
    "actions",
  ];

  toothFaces: ToothFaceInterface[] = ToothFaceFactory.createToothFaces();

  constructor(private snackBar: MatSnackBar) {
    this._loadForm();
    if (this.data.treatments?.length > 0) {
      this.treatmentsList = [...this.data.treatments];
    }
  }

  ngOnInit() {
    this.treatmentForm
      .get("treatment")
      ?.valueChanges.subscribe((treatmentName) => {
        const treatment = this.treatments.find((t) => t.name === treatmentName);
        if (treatment) {
          this.selectedTreatment = treatment;

          const currentType = this.treatmentForm.get("treatmentType")?.value;
          if (!treatment.availableTypes.includes(currentType)) {
            this.treatmentForm
              .get("treatmentType")
              ?.setValue(treatment.availableTypes[0]);
          }

          this._cleanupCariesControls();
          this.treatmentForm.removeControl("bridgeStart");
          this.treatmentForm.removeControl("bridgeEnd");

          if (treatment.name === TreatmentEnum.PUENTE) {
            this.treatmentForm.addControl(
              "bridgeStart",
              new FormControl(this.data.toothNumber)
            );
            this.treatmentForm.addControl("bridgeEnd", new FormControl(""));
          } else if (
            treatment.name === TreatmentEnum.CARIES ||
            treatment.name === TreatmentEnum.OBT_COMPOSITE
          ) {
            this._initializeCariesControls();
          }
        }
      });
  }

  isTypeAvailable(type: TreatmentTypeEnum): boolean {
    return this.selectedTreatment.availableTypes.includes(type);
  }

  onFaceChange(faceId: string, checked: boolean) {
    if (!checked && this.treatmentForm.get("allFaces")?.value) {
      this.treatmentForm.get("allFaces")?.setValue(false, { emitEvent: false });
    }

    const allSelected = this.toothFaces.every(
      (face) => this.treatmentForm.get(`face_${face.face}`)?.value
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
        SnackbarTypeEnum.Info
      );
      return false;
    }

    if (startIsUpper !== endIsUpper) {
      this.snackbarService.openSnackbar(
        "Los puentes solo pueden conectarse entre dientes de la misma línea (superior o inferior)",
        6000,
        "center",
        "top",
        SnackbarTypeEnum.Info
      );
      return false;
    }

    if (endTooth <= 0) {
      this.snackbarService.openSnackbar(
        "Debe seleccionar un diente válido",
        6000,
        "center",
        "top",
        SnackbarTypeEnum.Info
      );
      return false;
    }

    if (startTooth === endTooth) {
      this.snackbarService.openSnackbar(
        "El puente debe conectarse a un diente diferente",
        6000,
        "center",
        "top",
        SnackbarTypeEnum.Info
      );
      return false;
    }

    return true;
  }

  addTreatment() {
    const formValue = this.treatmentForm.value;
    const treatment: TreatmentInterfaceOld = {
      name: formValue.treatment,
      label: this.selectedTreatment.label,
      treatmentType: formValue.treatmentType,
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
      formValue.treatment === TreatmentEnum.OBT_COMPOSITE
    ) {
      const selectedFaces = this.toothFaces
        .filter((face) => formValue[`face_${face.face}`])
        .map((face) => face.face);

      treatment.faces = selectedFaces.length > 0 ? selectedFaces : undefined;
    }

    this.treatmentsList.push(treatment);
    this.treatmentsList = [...this.treatmentsList];
    this.treatmentForm.reset({
      treatment: this.treatments[0].name,
      treatmentType: this.treatments[0].availableTypes[0],
    });
  }

  removeTreatment(index: number) {
    this.treatmentsList.splice(index, 1);
    this.treatmentsList = [...this.treatmentsList];
  }

  private _loadForm() {
    this.treatmentForm = new FormGroup({
      treatment: new FormControl(this.treatments[0].name),
      treatmentType: new FormControl(this.treatments[0].availableTypes[0]),
    });

    if (
      this.treatments[0].name === TreatmentEnum.CARIES ||
      this.treatments[0].name === TreatmentEnum.OBT_COMPOSITE
    ) {
      this._initializeCariesControls();
    }
  }

  private _initializeCariesControls() {
    this.treatmentForm.addControl("allFaces", new FormControl(false));
    this.toothFaces.forEach((face) => {
      this.treatmentForm.addControl(
        `face_${face.face}`,
        new FormControl(false)
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
