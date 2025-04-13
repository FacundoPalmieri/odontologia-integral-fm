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
import {
  DisseaseConditionInterface,
  DisseaseTypeInterface,
} from "../../../domain/interfaces/clinical-history.interface";
import { DisseaseFactory } from "../../../utils/factories/dissease.factory";
import { DisseaseEnum } from "../../../utils/enums/dissease.enum";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-clinical-history",
  templateUrl: "./clinical-history.component.html",
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
export class ClincalHistoryComponent implements OnInit {
  clinicalHistoryForm: FormGroup = new FormGroup({});
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
    this.clinicalHistoryForm = new FormGroup({
      clinicHistories: new FormArray([]),
    });
  }

  private _createClinicHistoryFormGroup(): FormGroup {
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

  addClinicHistory(): void {
    this.clinicHistories.push(this._createClinicHistoryFormGroup());
  }

  removeClinicHistory(index: number): void {
    this.clinicHistories.removeAt(index);
  }

  get clinicHistories(): FormArray {
    return this.clinicalHistoryForm.get("clinicHistories") as FormArray;
  }

  getClinicHistoryFormGroup(index: number): FormGroup {
    return this.clinicHistories.controls[index] as FormGroup;
  }

  getDisseaseControl(index: number): FormControl {
    return this.getClinicHistoryFormGroup(index).controls[
      "dissease"
    ] as FormControl;
  }

  getMedicamentControl(index: number): FormControl {
    return this.getClinicHistoryFormGroup(index).controls[
      "medicament"
    ] as FormControl;
  }

  getConditionControl(index: number): FormControl {
    return this.getClinicHistoryFormGroup(index).controls[
      "condition"
    ] as FormControl;
  }

  getTypeControl(index: number): FormControl {
    return this.getClinicHistoryFormGroup(index).controls[
      "type"
    ] as FormControl;
  }

  getDescriptionControl(index: number): FormControl {
    return this.getClinicHistoryFormGroup(index).controls[
      "description"
    ] as FormControl;
  }
}
