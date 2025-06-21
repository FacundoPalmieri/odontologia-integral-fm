import { Component, inject, Input, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatIconModule } from "@angular/material/icon";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PatientService } from "../../../services/patient.service";
import { map, Observable, of, startWith, Subject, takeUntil } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../domain/interfaces/api-response.interface";
import { MatInputModule } from "@angular/material/input";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatDialog } from "@angular/material/dialog";
import { CreatePatientDialogComponent } from "../create-patient-dialog/create-patient-dialog.component";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { PatientDtoInterface } from "../../../domain/dto/patient.dto";

@Component({
  selector: "app-patient-search",
  templateUrl: "./patient-search.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatToolbarModule,
    MatInputModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export class PatientSearchComponent {
  @Input() showCreatePatientButton: boolean = false;

  private readonly _destroy$ = new Subject<void>();

  patientService = inject(PatientService);
  dialog = inject(MatDialog);
  patients = signal<PatientDtoInterface[]>([]);

  patientSearchControl = new FormControl("");
  filteredPatients: Observable<PatientDtoInterface[]> = of([]);
  selectedPatient: PatientDtoInterface | null = null;

  constructor() {
    this._getPatients();

    this.filteredPatients = this.patientSearchControl.valueChanges.pipe(
      startWith(""),
      map((value) => {
        return this._filterPatients(value || "");
      })
    );
  }

  displayFn(patient: PatientDtoInterface): string {
    return patient && patient.person.firstName && patient.person.lastName
      ? patient.person.firstName + " " + patient.person.lastName
      : "";
  }

  onPatientSelected(patient: PatientDtoInterface): void {
    this.selectedPatient = patient;

    this.filteredPatients = this.patientSearchControl.valueChanges.pipe(
      startWith(""),
      map((value) => {
        return this._filterPatients(value || "");
      })
    );
  }

  private _filterPatients(value: string): PatientDtoInterface[] {
    const filterValue = value.toLowerCase();

    return this.patients().filter((patient) => {
      const firstName = patient.person.firstName.toLowerCase();
      const lastName = patient.person.lastName.toLowerCase();
      const dni = patient.person.dni.toString();

      return (
        firstName.includes(filterValue) ||
        lastName.includes(filterValue) ||
        dni.includes(filterValue)
      );
    });
  }

  create() {
    this.dialog.open(CreatePatientDialogComponent);
  }

  private _getPatients() {
    this.patientService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<
            PagedDataInterface<PatientDtoInterface[]>
          >
        ) => {
          if (response.success) {
            this.patients.set(response.data.content);
          }
        }
      );
  }
}
