import { Component, effect, inject, signal, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { MatCardModule } from "@angular/material/card";
import { PatientSearchComponent } from "../../components/patient-search/patient-search.component";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { AppointmentService } from "../../../services/appointment.service";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";

@Component({
  selector: "app-appointments",
  templateUrl: "./appointments.component.html",
  styleUrl: "./appointments.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    PatientSearchComponent,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
  ],
})
export class AppointmentsComponent {
  private readonly appointmentService = inject(AppointmentService);
  @ViewChild(MatPaginator)
  paginator!: MatPaginator;
  @ViewChild(MatSort)
  sort!: MatSort;

  appointmentsDataSource: MatTableDataSource<any> = new MatTableDataSource();
  appointments = signal<any[]>([]);
  displayedColumns = [
    "firstname",
    "lastname",
    "date",
    "professional",
    "status",
    "action",
  ];

  constructor() {
    this._loadData();
    effect(() => {
      if (this.appointments()) {
        this.appointmentsDataSource.data = this.appointments();
        this.appointmentsDataSource.paginator = this.paginator;
        this.appointmentsDataSource.sort = this.sort;
      }
    });
  }

  toggleWaiting() {
    this.appointments.set(this.appointmentService.getWaiting());
  }

  toggleInProgress() {
    this.appointments.set(this.appointmentService.getInProgress());
  }

  togglePendingPayment() {
    this.appointments.set(this.appointmentService.getPendingPayment());
  }

  toggleScheduled() {
    this.appointments.set(this.appointmentService.getScheduled());
  }

  toggleFinalized() {
    this.appointments.set(this.appointmentService.getFinalized());
  }

  toggleCanceled() {
    this.appointments.set(this.appointmentService.getCanceled());
  }

  private _loadData() {
    this.appointments.set(this.appointmentService.getScheduled());
    this.appointmentsDataSource.paginator = this.paginator;
    this.appointmentsDataSource.sort = this.sort;
  }
}
