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
import { CreatePatientDialogComponent } from "../../components/create-patient-dialog/create-patient-dialog.component";
import { mockOdontogram } from "../../../utils/mocks/odontogram.mock";

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

  patients: PatientInterface[] = [
    {
      firstName: "María",
      lastName: "Pérez",
      dniType: {
        id: 1,
        dni: "DNI",
      },
      dni: "40999888",
      birthDate: new Date(1998, 2, 11),
      gender: {
        id: 2,
        alias: "Mujer",
      },
      nationality: {
        id: 1,
        name: "Argentina",
      },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 2,
        name: "Buenos Aires",
      },
      locality: {
        id: 1,
        name: "San Justo",
      },
      street: "Av. Siempre Viva",
      number: 742,
      floor: "3",
      apartment: "B",
      healthPlan: {
        id: 2,
        name: "Swiss Medical",
      },
      affiliateNumber: "A123456789",
      email: "maria.perez@example.com",
      phoneType: {
        id: 1,
        name: "Celular",
      },
      phone: "1123456789",
    },
    {
      firstName: "Carlos",
      lastName: "Rodríguez",
      dniType: { id: 1, dni: "DNI" },
      dni: "38765432",
      birthDate: new Date(1985, 3, 8),
      gender: { id: 1, alias: "Hombre" },
      nationality: { id: 1, name: "Argentina" },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 1,
        name: "CABA",
      },
      locality: { id: 1, name: "San Telmo" },
      street: "Calle de la Luna",
      number: 789,
      floor: "4",
      apartment: "D",
      healthPlan: { id: 2, name: "Swiss Medical" },
      affiliateNumber: "S789123456",
      email: "carlos.rodriguez@example.com",
      phoneType: { id: 2, name: "Fijo" },
      phone: "1156789123",
    },
    {
      firstName: "Laura",
      lastName: "Martínez",
      dniType: { id: 1, dni: "DNI" },
      dni: "45678901",
      birthDate: new Date(2002, 7, 12),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 1,
        name: "CABA",
      },
      locality: { id: 4, name: "Villa Urquiza" },
      street: "Pasaje Estrella",
      number: 246,
      floor: "PB",
      apartment: "N/A",
      healthPlan: { id: 1, name: "OSDE" },
      affiliateNumber: "O246813579",
      email: "laura.martinez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "2611357924",
    },
    {
      firstName: "Ana",
      lastName: "Gómez",
      dniType: { id: 1, dni: "DNI" },
      dni: "25123456",
      birthDate: new Date(1978, 11, 20),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 1,
        name: "CABA",
      },
      locality: { id: 3, name: "Palermo" },
      street: "Avenida Siempreviva",
      number: 1234,
      floor: "PB",
      apartment: "A",
      healthPlan: { id: 1, name: "OSDE" },
      affiliateNumber: "O987654321",
      email: "ana.gomez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "1511223344",
    },
    {
      firstName: "Martín",
      lastName: "Pérez",
      dniType: { id: 2, dni: "LC" },
      dni: "1234567",
      birthDate: new Date(1965, 11, 3),
      gender: { id: 1, alias: "Hombre" },
      nationality: { id: 2, name: "Uruguay" },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 1,
        name: "CABA",
      },
      locality: { id: 5, name: "Belgrano" },
      street: "Calle Falsa",
      number: 567,
      floor: "1",
      apartment: "B",
      healthPlan: { id: 3, name: "Medifé" },
      affiliateNumber: "M654321987",
      email: "martin.perez@example.com",
      phoneType: { id: 2, name: "Fijo" },
      phone: "47778899",
    },
    {
      firstName: "Laura",
      lastName: "Fernández",
      dniType: { id: 1, dni: "DNI" },
      dni: "41987654",
      birthDate: new Date(1990, 9, 1),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 1,
        name: "CABA",
      },
      locality: { id: 2, name: "Recoleta" },
      street: "Pasaje Secreto",
      number: 234,
      floor: "2",
      apartment: "C",
      healthPlan: { id: 2, name: "Swiss Medical" },
      affiliateNumber: "S112233445",
      email: "laura.fernandez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "1599887766",
    },
    {
      firstName: "Javier",
      lastName: "López",
      dniType: { id: 1, dni: "DNI" },
      dni: "30543210",
      birthDate: new Date(1970, 12, 25),
      gender: { id: 1, alias: "Hombre" },
      nationality: { id: 3, name: "Argentina" },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 2,
        name: "Buenos Aires",
      },
      locality: { id: 4, name: "San Nicolás" },
      street: "Avenida Principal",
      number: 1010,
      floor: "5",
      apartment: "E",
      healthPlan: { id: 1, name: "OSDE" },
      affiliateNumber: "O556677889",
      email: "javier.lopez@example.com",
      phoneType: { id: 2, name: "Fijo" },
      phone: "43332211",
    },
    {
      firstName: "Sofía",
      lastName: "Martínez",
      dniType: { id: 1, dni: "DNI" },
      dni: "45678901",
      birthDate: new Date(1995, 7, 10),
      gender: { id: 2, alias: "Mujer" },
      nationality: { id: 1, name: "Argentina" },
      country: {
        id: 1,
        name: "Argentina",
      },
      province: {
        id: 1,
        name: "CABA",
      },
      locality: { id: 1, name: "San Telmo" },
      street: "Calle Antigua",
      number: 321,
      floor: "3",
      apartment: "F",
      healthPlan: { id: 3, name: "Medifé" },
      affiliateNumber: "M998877665",
      email: "sofia.martinez@example.com",
      phoneType: { id: 1, name: "Celular" },
      phone: "1544556677",
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
  odontogram = mockOdontogram;

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

    return this.patients.filter((patient) => {
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
}
