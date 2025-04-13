import { Component, inject, OnInit } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatDatepickerModule } from "@angular/material/datepicker";
import {
  LocalityFactory,
  LocalityInterface,
} from "../../../utils/factories/locality.factory";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { map, Observable, startWith } from "rxjs";
import { AsyncPipe } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatTooltipModule } from "@angular/material/tooltip";
import { ClincalHistoryComponent } from "../clinical-history/clinical-history.component";
import { MatExpansionModule } from "@angular/material/expansion";

@Component({
  selector: "app-create-patient-dialog",
  templateUrl: "./create-patient-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    AsyncPipe,
    IconsModule,
    MatTooltipModule,
    ClincalHistoryComponent,
    MatExpansionModule,
  ],
})
export class CreatePatientDialogComponent implements OnInit {
  dialogRef = inject(MatDialogRef<CreatePatientDialogComponent>);
  patientForm: FormGroup = new FormGroup({});
  localities: LocalityInterface[] = LocalityFactory.createLocalities();
  filteredLocalities: Observable<LocalityInterface[]> | undefined;
  localityControl = new FormControl<string | LocalityInterface>("");

  constructor() {
    this._loadForm();
    this._loadData();
  }

  ngOnInit() {
    this.filteredLocalities = this.patientForm.controls[
      "locality"
    ].valueChanges.pipe(
      startWith(""),
      map((value) => {
        const name = typeof value === "string" ? value : value?.district;
        return name
          ? this._filterLocality(name as string)
          : this.localities.slice();
      })
    );
  }

  displayFn(locality: LocalityInterface): string {
    return locality && locality.district ? locality.district : "";
  }

  private _loadForm() {
    this.patientForm = new FormGroup({
      name: new FormControl<string>("", [Validators.required]),
      birthday: new FormControl<Date>(new Date(), [Validators.required]),
      dni: new FormControl<number | null>(null, [Validators.required]),
      phone: new FormControl<number | null>(null),
      mail: new FormControl<string>("", [Validators.email]),
      address: new FormControl<string>(""),
      locality: new FormControl<LocalityInterface | null>(null),
      medicare: new FormControl<string>(""),
      affiliateNumber: new FormControl<number | null>(null),
      medicalHistory: new FormArray([]),
    });
  }

  private _loadData() {}

  private _filterLocality(value: string): LocalityInterface[] {
    const filterValue = value.toLowerCase();

    return this.localities.filter((locality) =>
      locality.district.toLowerCase().includes(filterValue)
    );
  }
}
