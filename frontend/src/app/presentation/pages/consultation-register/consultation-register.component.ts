import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatStepperModule } from "@angular/material/stepper";
import { STEPPER_GLOBAL_OPTIONS } from "@angular/cdk/stepper";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { OdontogramComponent } from "../../components/odontogram/odontogram.component";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatDialog } from "@angular/material/dialog";
import { mockOdontogram } from "../../../utils/mocks/odontogram.mock";
import { PatientSearchComponent } from "../../components/patient-search/patient-search.component";
import { PaymentRegisterComponent } from "../../components/payment-register/payment-register.component";
import { MatInputModule } from "@angular/material/input";

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
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    OdontogramComponent,
    PatientSearchComponent,
    PaymentRegisterComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
  ],
})
export class ConsultationRegisterComponent implements OnInit {
  readonly dialog = inject(MatDialog);
  patientForm: FormGroup = new FormGroup({
    observations: new FormControl(""),
  });

  todayDate: Date = new Date();
  observations: string = "";
  odontogram = mockOdontogram;

  ngOnInit() {
    this.patientForm.get("observations")?.valueChanges.subscribe((value) => {
      this.observations = value || "";
    });
  }

  finalizeConsultation() {}
}
