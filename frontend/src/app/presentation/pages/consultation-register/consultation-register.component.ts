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

interface PatientInterface {
  name: string;
  dni: string;
  phone: number;
  mail: string;
}

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
  ],
})
export class ConsultationRegisterComponent implements OnInit {
  patientSearchControl = new FormControl("");
  filteredPatients: Observable<PatientInterface[]> = of([]);

  patientForm: FormGroup = new FormGroup({});

  patients: PatientInterface[] = [
    {
      name: "Matías Iglesias",
      dni: "12341234",
      phone: 1112312312,
      mail: "matias@iglesias.com",
    },
    {
      name: "Facundo Palmieri",
      dni: "56785678",
      phone: 1145645666,
      mail: "facundo@pamieri.com",
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
        patient.dni.includes(filterValue)
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
