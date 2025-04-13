import { Component, inject, OnInit } from "@angular/core";
import { AsyncPipe, CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatStepperModule } from "@angular/material/stepper";
import { STEPPER_GLOBAL_OPTIONS } from "@angular/cdk/stepper";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
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
import { MatSelectModule } from "@angular/material/select";
import { PaymentMethodEnum } from "../../../utils/enums/payment-method.enum";
import { MatDialog } from "@angular/material/dialog";
import { CreatePatientDialogComponent } from "../../components/patient-create-dialog/create-patient-dialog.component";
import { ClincalHistoryComponent } from "../../components/clinical-history/clinical-history.component";

@Component({
  selector: "app-consultation-register",
  templateUrl: "./consultation-register.component.html",
  styleUrl: "./consultation-register.component.scss",
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
    MatSelectModule,
    ClincalHistoryComponent,
  ],
})
export class ConsultationRegisterComponent implements OnInit {
  readonly dialog = inject(MatDialog);

  paymentMethodEnum = PaymentMethodEnum;
  patientSearchControl = new FormControl("");
  filteredPatients: Observable<PatientInterface[]> = of([]);

  patientForm: FormGroup = new FormGroup({
    observations: new FormControl(""),
  });
  paymentForm: FormGroup = new FormGroup({
    paymentMethod: new FormControl("", Validators.required),
    totalAmount: new FormControl(null, Validators.required),
    installments: new FormControl(1),
  });
  icons = ["letter-t-small", "letter-c-small"];

  patients: PatientInterface[] = [
    {
      name: "Matías Iglesias",
      birthday: new Date(1998, 2, 11),
      dni: 12341234,
      phone: 1112312312,
      mail: "matias@iglesias.com",
      address: "Tucuman 752",
      locality: "CABA",
      medicare: "Swiss Medical",
      affiliateNumber: 123456787654321,
    },
    {
      name: "Facundo Palmieri",
      birthday: new Date(1993, 4, 17),
      dni: 12341234,
      phone: 1112312312,
      mail: "facundo@palmieri.com",
      address: "Tucuman 752",
      locality: "CABA",
      medicare: "Galeno",
      affiliateNumber: 123456787654321,
    },
    {
      name: "Ana García",
      birthday: new Date(1982, 8, 22),
      dni: 56785678,
      phone: 1198798798,
      mail: "ana@garcia.com",
      address: "Corrientes 123",
      locality: "CABA",
      medicare: "OSDE",
      affiliateNumber: 987654321234567,
    },
    {
      name: "Matías Rodríguez",
      birthday: new Date(1989, 11, 5),
      dni: 90129012,
      phone: 1145645645,
      mail: "matias@rodriguez.com",
      address: "Santa Fe 456",
      locality: "CABA",
      medicare: "Medifé",
      affiliateNumber: 543212349876543,
    },
    {
      name: "Facundo López",
      birthday: new Date(1995, 6, 10),
      dni: 34563456,
      phone: 1178978978,
      mail: "facundo@lopez.com",
      address: "Maipú 789",
      locality: "CABA",
      medicare: "Swiss Medical",
      affiliateNumber: 789123456543219,
    },
  ];

  treatmentsExample: { diente: string; tratamiento: string }[] = [
    { diente: "12", tratamiento: "Puente" },
    { diente: "24", tratamiento: "Extracción" },
    { diente: "36", tratamiento: "Implante" },
    { diente: "48", tratamiento: "Implante" },
    { diente: "15", tratamiento: "Extracción" },
  ];

  selectedPatient: PatientInterface | null = null;
  todayDate: Date = new Date();
  observations: string = "";

  ngOnInit() {
    this.filteredPatients = this.patientSearchControl.valueChanges.pipe(
      startWith(""),
      map((value) => {
        return this._filterPatients(value || "");
      })
    );

    this.patientForm.get("observations")?.valueChanges.subscribe((value) => {
      this.observations = value || "";
    });
  }

  openCreatePatientDialog() {
    const dialogRef = this.dialog.open(CreatePatientDialogComponent, {
      width: "1400px",
    });
  }

  displayFn(patient: PatientInterface): string {
    return patient && patient.name ? patient.name : "";
  }

  onPatientSelected(patient: PatientInterface): void {
    this.selectedPatient = patient;
    this.patientSearchControl.setValue("");
  }

  calculateInstallmentAmount(installments: number): string {
    const totalAmount = this.paymentForm.get("totalAmount")?.value;

    if (totalAmount && installments && installments !== 0) {
      const numericTotalAmount = parseFloat(totalAmount);
      if (!isNaN(numericTotalAmount)) {
        return (numericTotalAmount / installments).toLocaleString("es-AR", {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        });
      }
    }

    return "0.00";
  }

  finalizeConsultation() {}

  private _filterPatients(value: string): PatientInterface[] {
    const filterValue = value.toLowerCase();

    return this.patients.filter(
      (patient) =>
        patient.name.toLowerCase().includes(filterValue) ||
        patient.dni.toString().includes(filterValue)
    );
  }
}
