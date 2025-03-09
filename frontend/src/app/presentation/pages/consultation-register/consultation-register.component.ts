import { Component, OnInit } from "@angular/core";
import { AsyncPipe, CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatStepperModule } from "@angular/material/stepper";
import { STEPPER_GLOBAL_OPTIONS } from "@angular/cdk/stepper";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { map, Observable, of, startWith } from "rxjs";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatInputModule } from "@angular/material/input";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatTooltipModule } from "@angular/material/tooltip";
import { OdontogramComponent } from "../../components/odontogram/odontogram.component";
import { PatientInterface } from "../../../domain/interfaces/patient.interface";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";

@Component({
  selector: "app-consultation-register",
  templateUrl: "./consultation-register.component.html",
  standalone: true,
  providers: [
    {
      provide: STEPPER_GLOBAL_OPTIONS,
      useValue: { showError: true },
    },
  ],
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatStepperModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule,
    MatAutocompleteModule,
    MatInputModule,
    AsyncPipe,
    MatExpansionModule,
    MatTooltipModule,
    MatDatepickerModule,
    MatNativeDateModule,
    OdontogramComponent,
  ],
})
export class ConsultationRegisterComponent implements OnInit {
  patientSearchControl = new FormControl("");
  filteredPatients: Observable<PatientInterface[]> = of([]);

  patientForm: FormGroup = new FormGroup({});

  patients: PatientInterface[] = [
    {
      name: "Matías Iglesias",
      age: 26,
      birthday: new Date(1998, 2, 11),
      dni: 12341234,
      phone: 1112312312,
      mail: "matias@iglesias.com",
      address: "Tucuman 752",
      locality: "CABA",
      occupation: "Software Engineer",
      medicare: "Swiss Medical",
      affiliate_number: 123456787654321,
    },
    {
      name: "Facundo Palmieri",
      age: 31,
      birthday: new Date(1993, 4, 17),
      dni: 12341234,
      phone: 1112312312,
      mail: "facundo@palmieri.com",
      address: "Tucuman 752",
      locality: "CABA",
      occupation: "Software Engineer",
      medicare: "Galeno",
      affiliate_number: 123456787654321,
    },
  ];

  selectedPatient: PatientInterface | null = null;

  ngOnInit() {
    this.filteredPatients = this.patientSearchControl.valueChanges.pipe(
      startWith(""),
      map((value) => this._filterPatients(value || ""))
    );
  }

  private _filterPatients(value: string): PatientInterface[] {
    const filterValue = value.toLowerCase();

    return this.patients.filter(
      (patient) =>
        patient.name.toLowerCase().includes(filterValue) ||
        patient.dni.toString().includes(filterValue)
    );
  }

  displayFn(patient: PatientInterface): string {
    return patient && patient.name ? patient.name : "";
  }

  onPatientSelected(patient: PatientInterface): void {
    this.selectedPatient = patient;
    this.patientSearchControl.setValue("");
  }

  finalizeConsultation() {}
}
