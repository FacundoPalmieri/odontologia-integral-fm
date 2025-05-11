import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
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
import { MatInputModule } from "@angular/material/input";
import { MatTooltipModule } from "@angular/material/tooltip";
import { OdontogramComponent } from "../../components/odontogram/odontogram.component";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatSelectModule } from "@angular/material/select";
import { PaymentMethodEnum } from "../../../utils/enums/payment-method.enum";
import { MatDialog } from "@angular/material/dialog";
import { mockOdontogram } from "../../../utils/mocks/odontogram.mock";
import { PatientSearchComponent } from "../../components/patient-search/patient-search.component";

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
    MatInputModule,
    MatTooltipModule,
    MatDatepickerModule,
    MatNativeDateModule,
    OdontogramComponent,
    MatSelectModule,
    PatientSearchComponent,
  ],
})
export class ConsultationRegisterComponent implements OnInit {
  readonly dialog = inject(MatDialog);
  paymentMethodEnum = PaymentMethodEnum;
  patientForm: FormGroup = new FormGroup({
    observations: new FormControl(""),
  });
  paymentForm: FormGroup = new FormGroup({
    paymentMethod: new FormControl("", Validators.required),
    totalAmount: new FormControl(null, Validators.required),
    installments: new FormControl(1),
  });

  todayDate: Date = new Date();
  observations: string = "";
  odontogram = mockOdontogram;

  ngOnInit() {
    this.patientForm.get("observations")?.valueChanges.subscribe((value) => {
      this.observations = value || "";
    });
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
}
