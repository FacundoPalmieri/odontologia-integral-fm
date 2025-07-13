import { Component, inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MedicalHistoryRiskInterface } from "../../../../../domain/interfaces/patient.interface";
import { MatIconModule } from "@angular/material/icon";
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { PersonDataService } from "../../../../../services/person-data.service";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { PatientService } from "../../../../../services/patient.service";
import { Subject, takeUntil } from "rxjs";
import { SnackbarService } from "../../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../utils/enums/snackbar-type.enum";

@Component({
  selector: "app-add-medical-risk-dialog",
  templateUrl: "./add-medical-risk-dialog.component.html",
  standalone: true,
  imports: [
    MatIconModule,
    IconsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatButtonModule,
    MatDialogModule,
  ],
})
export class AddMedicalRiskDialogComponent implements OnInit {
  private readonly _destroy$ = new Subject<void>();
  private readonly snackbarService = inject(SnackbarService);
  private readonly patientService = inject(PatientService);

  dialogRef = inject(MatDialogRef<AddMedicalRiskDialogComponent>);
  data = inject(MAT_DIALOG_DATA);
  personDataService = inject(PersonDataService);
  medicalRisksForm: FormGroup;

  constructor() {
    this.medicalRisksForm = new FormGroup({
      medicalRisks: new FormArray([]),
    });
  }

  ngOnInit(): void {
    console.log(this.data.medicalRisks);
    if (this.data.medicalRisks && this.data.medicalRisks.length > 0) {
      this.data.medicalRisks.forEach((risk: MedicalHistoryRiskInterface) => {
        this.medicalRisks.push(
          new FormGroup({
            selectedRisk: new FormControl<MedicalHistoryRiskInterface | null>(
              { id: risk.id, name: risk.name },
              [Validators.required]
            ),
            observation: new FormControl<string>(risk.observation || "", [
              Validators.maxLength(500),
            ]),
          })
        );
      });
    } else if (this.data.medicalRisks.length === 0) {
      this.addMedicalRisk();
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  addMedicalRisk() {
    this.medicalRisks.push(
      new FormGroup({
        selectedRisk: new FormControl<MedicalHistoryRiskInterface | null>(
          null,
          [Validators.required]
        ),
        observation: new FormControl<string>("", [Validators.maxLength(500)]),
      })
    );
  }

  removeMedicalRisk(index: number) {
    this.medicalRisks.removeAt(index);
  }

  submit() {
    if (this.medicalRisksForm.valid) {
      const medicalRisks = this.medicalRisksForm.value.medicalRisks.map(
        (medicalRisk: any) => {
          return {
            medicalRiskId: medicalRisk.selectedRisk.id,
            observation: medicalRisk.observation,
          };
        }
      );

      this.patientService
        .updateMedicalRisks(this.data.patientId, medicalRisks)
        .pipe(takeUntil(this._destroy$))
        .subscribe({
          next: () => {
            this.snackbarService.openSnackbar(
              "Antecentes clínicos actualizados.",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
            this.dialogRef.close(this.medicalRisksForm.value.medicalRisks);
          },
          error: () => {
            this.snackbarService.openSnackbar(
              "Error al actualizar los antecedentes clínicos.",
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Error
            );
          },
        });
    }
  }

  get medicalRisks(): FormArray {
    return this.medicalRisksForm.get("medicalRisks") as FormArray;
  }

  close() {
    this.dialogRef.close();
  }

  compare = (a: MedicalHistoryRiskInterface, b: MedicalHistoryRiskInterface) =>
    a && b && a.id === b.id;
}
