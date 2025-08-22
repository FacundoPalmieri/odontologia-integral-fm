import { Component } from "@angular/core";
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
import { PaymentMethodEnum } from "../../../utils/enums/payment-method.enum";
import { MatInputModule } from "@angular/material/input";

@Component({
  selector: "app-payment-register",
  templateUrl: "./payment-register.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
  ],
})
export class PaymentRegisterComponent {
  paymentMethodEnum = PaymentMethodEnum;
  paymentForm: FormGroup = new FormGroup({
    paymentMethod: new FormControl("", Validators.required),
    totalAmount: new FormControl(null, Validators.required),
    installments: new FormControl(1),
    operationNumber: new FormControl(""),
  });
  constructor() {}

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
}
