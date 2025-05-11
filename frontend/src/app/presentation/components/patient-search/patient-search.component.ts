import { Component, inject, Input, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatIconModule } from "@angular/material/icon";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PatientService } from "../../../services/patient.service";
import { PatientInterface } from "../../../domain/interfaces/patient.interface";
import { map, Observable, of, startWith, Subject, takeUntil } from "rxjs";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatInputModule } from "@angular/material/input";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatDialog } from "@angular/material/dialog";
import { CreatePatientDialogComponent } from "../create-patient-dialog/create-patient-dialog.component";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";

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
  patients = signal<PatientInterface[]>([]);

  patientSearchControl = new FormControl("");
  filteredPatients: Observable<PatientInterface[]> = of([]);
  selectedPatient: PatientInterface | null = null;

  constructor() {
    this._getPatients();

    this.filteredPatients = this.patientSearchControl.valueChanges.pipe(
      startWith(""),
      map((value) => {
        return this._filterPatients(value || "");
      })
    );
  }

  displayFn(patient: PatientInterface): string {
    return patient && patient.firstName && patient.lastName
      ? patient.firstName + " " + patient.lastName
      : "";
  }

  onPatientSelected(patient: PatientInterface): void {
    this.selectedPatient = patient;

    this.filteredPatients = this.patientSearchControl.valueChanges.pipe(
      startWith(""),
      map((value) => {
        return this._filterPatients(value || "");
      })
    );
  }

  private _filterPatients(value: string): PatientInterface[] {
    const filterValue = value.toLowerCase();

    return this.patients().filter((patient) => {
      const firstName = patient.firstName.toLowerCase();
      const lastName = patient.lastName.toLowerCase();
      const dni = patient.dni.toString();

      return (
        firstName.includes(filterValue) ||
        lastName.includes(filterValue) ||
        dni.includes(filterValue)
      );
    });
  }

  create() {
    const dialogRef = this.dialog.open(CreatePatientDialogComponent, {
      width: "1400px",
    });
  }

  private _getPatients() {
    this.patientService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<PatientInterface[]>) => {
        if (response.success) {
          this.patients.set(response.data);
        }
      });
  }
}
