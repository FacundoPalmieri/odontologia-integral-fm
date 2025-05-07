import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatSelectModule } from "@angular/material/select";
import { MatInputModule } from "@angular/material/input";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { DisseaseFactory } from "../../../utils/factories/dissease.factory";
import { DisseaseEnum } from "../../../utils/enums/dissease.enum";
import { MatTooltipModule } from "@angular/material/tooltip";
import {
  DisseaseConditionInterface,
  DisseaseTypeInterface,
} from "../../../domain/interfaces/patient.interface";

@Component({
  selector: "app-medical-risks",
  templateUrl: "./medical-risks.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export class MedicalRisksComponent implements OnInit {
  medicalRisksForm: FormGroup = new FormGroup({});
  disseaseTypes: DisseaseTypeInterface[] =
    DisseaseFactory.createDisseaseTypes();
  disseaseConditions: DisseaseConditionInterface[] =
    DisseaseFactory.createDisseaseConditions();
  disseaseEnum = DisseaseEnum;

  constructor() {}

  ngOnInit(): void {
    this._loadForm();
  }

  private _loadForm() {
    this.medicalRisksForm = new FormGroup({
      medicalRisks: new FormArray([]),
    });
  }

  private _createMedicalRiskFormGroup(): FormGroup {
    return new FormGroup({
      dissease: new FormControl<DisseaseTypeInterface | null>(null, [
        Validators.required,
      ]),
      condition: new FormControl<DisseaseConditionInterface[] | null>(null),
      medicament: new FormControl<string>(""),
      type: new FormControl<string>(""),
      description: new FormControl<string>(""),
    });
  }

  addMedicalRisk(): void {
    this.medicalRisks.push(this._createMedicalRiskFormGroup());
  }

  removeMedicalRisk(index: number): void {
    this.medicalRisks.removeAt(index);
  }

  get medicalRisks(): FormArray {
    return this.medicalRisksForm.get("medicalRisks") as FormArray;
  }

  getMedicalRisksFormGroup(index: number): FormGroup {
    return this.medicalRisks.controls[index] as FormGroup;
  }

  getDisseaseControl(index: number): FormControl {
    return this.getMedicalRisksFormGroup(index).controls[
      "dissease"
    ] as FormControl;
  }

  getMedicamentControl(index: number): FormControl {
    return this.getMedicalRisksFormGroup(index).controls[
      "medicament"
    ] as FormControl;
  }

  getConditionControl(index: number): FormControl {
    return this.getMedicalRisksFormGroup(index).controls[
      "condition"
    ] as FormControl;
  }

  getTypeControl(index: number): FormControl {
    return this.getMedicalRisksFormGroup(index).controls["type"] as FormControl;
  }

  getDescriptionControl(index: number): FormControl {
    return this.getMedicalRisksFormGroup(index).controls[
      "description"
    ] as FormControl;
  }
}
