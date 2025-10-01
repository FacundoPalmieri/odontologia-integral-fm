import { Component, inject, Input } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { PersonDataService } from "../../../services/person-data.service";
import { MatInputModule } from "@angular/material/input";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { Subject } from "rxjs";
import {
  DentistInterface,
  DentistSpecialtyInterface,
} from "../../../domain/interfaces/dentist.interface";

@Component({
  selector: "app-dentist-form",
  templateUrl: "./dentist-form.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatDatepickerModule,
  ],
})
export class DentistFormComponent {
  private _dentist!: DentistInterface;

  @Input() readonly = false;

  @Input({ required: true })
  set dentist(value: DentistInterface) {
    this._dentist = value;
    if (value) {
      this.dentistForm.patchValue(value);
    }
  }

  personDataService = inject(PersonDataService);

  dentistForm: FormGroup = new FormGroup({
    licenseNumber: new FormControl<string | null>("", [
      Validators.required,
      Validators.maxLength(30),
    ]),
    dentistSpecialty: new FormControl<DentistSpecialtyInterface | null>(null, [
      Validators.required,
    ]),
  });

  constructor() {}

  compare = (
    item1: DentistSpecialtyInterface | null,
    item2: DentistSpecialtyInterface | null
  ): boolean => {
    return item1 && item2 ? item1.id === item2.id : item1 === item2;
  };

  get dentist(): DentistInterface {
    return this._dentist;
  }
}
