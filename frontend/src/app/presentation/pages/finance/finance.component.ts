import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatCardModule } from "@angular/material/card";
import { BaseChartDirective } from "ng2-charts";
import { ChartConfiguration } from "chart.js";
import { MatSelectModule } from "@angular/material/select";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { PaymentMethodEnum } from "../../../utils/enums/payment-method.enum";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatListModule } from "@angular/material/list";

@Component({
  selector: "app-finance",
  templateUrl: "./finance.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    BaseChartDirective,
    MatSelectModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatListModule,
  ],
})
export class FinanceComponent {
  paymentMethodEnum = PaymentMethodEnum;
  paymentForm: FormGroup = new FormGroup({
    paymentMethod: new FormControl("", Validators.required),
    totalAmount: new FormControl(null, Validators.required),
    installments: new FormControl(1),
  });

  summaryCards = [
    {
      title: "Total facturado (Mes)",
      amount: 15200,
      variation: 15.6,
      icon: "cash-register",
    },
    {
      title: "Total facturado (Año)",
      amount: 96800,
      variation: 8.2,
      icon: "cash-register",
    },
    {
      title: "Total en efectivo",
      amount: 32150,
      variation: 0,
      icon: "cash",
    },
    {
      title: "Total con tarjeta",
      amount: 5100,
      variation: 0,
      icon: "credit-card",
    },
    {
      title: "Total con transferencia",
      amount: 12600,
      variation: -2.1,
      icon: "credit-card-pay",
    },
  ];

  lineChartOptions: ChartConfiguration["options"] = {
    responsive: true,
    plugins: {
      legend: {
        display: false,
      },
      title: {
        display: false,
      },
      tooltip: {
        enabled: true,
        position: "nearest",
      },
    },
  };

  lineChartLabels = [
    "Ene",
    "Feb",
    "Mar",
    "Abr",
    "May",
    "Jun",
    "Jul",
    "Ago",
    "Sep",
    "Oct",
    "Nov",
    "Dic",
  ];

  lineChartData = {
    labels: this.lineChartLabels,
    datasets: [
      {
        data: [
          1200000, 1900000, 3000000, 2500000, 2700000, 3100000, 3300000,
          2900000, 3400000, 3600000, 3900000, 4100000,
        ],
        label: "Facturación",
        fill: false,
        borderColor: "rgba(16, 185, 129)",
        backgroundColor: "rgba(16, 185, 129)",
        tension: 0.5,
      },
    ],
  };

  barChartOptions: ChartConfiguration["options"] = {
    responsive: true,
    indexAxis: "y",
    plugins: {
      legend: { display: false },
      title: { display: false },
    },
  };

  barChartData = {
    labels: ["Efectivo", "Tarjeta", "Transferencia"],
    datasets: [
      {
        data: [5200000, 2800000, 2000000],
        backgroundColor: [
          "rgba(16, 185, 129, 0.3)",
          "rgba(59, 130, 246, 0.3)",
          "rgba(245, 158, 11, 0.3)",
        ],
        borderColor: ["#10b981", "#3b82f6", "#f59e0b"],
        borderWidth: 2,
        borderRadius: 8,
        barThickness: 40,
      },
    ],
  };

  recentPayments = [
    { patient: "Juan Pérez", method: "Efectivo", amount: 52000 },
    { patient: "Ana López", method: "Tarjeta", amount: 28000 },
    { patient: "Carlos Ruiz", method: "Transferencia", amount: 20000 },
    { patient: "Sofía Méndez", method: "Efectivo", amount: 15000 },
    { patient: "Mariano Díaz", method: "Tarjeta", amount: 34000 },
  ];
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

  paymentRegister() {
    // Registra el cobro en la base de datos y luego resetea el form
    this.paymentForm.reset({
      paymentMethod: "",
      totalAmount: null,
      installments: 1,
    });
  }
}
