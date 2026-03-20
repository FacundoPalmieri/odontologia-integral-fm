import { Component, effect, inject, signal, ViewChild } from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { MatCardModule } from "@angular/material/card";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { AppointmentService } from "../../../services/appointment.service";
import { RoleEnum } from "../../../../../shared/utils/enums/role.enum";
import { MatDialogModule, MatDialog } from "@angular/material/dialog";
import { CreateAppointmentDialogComponent } from "../../../../calendar/presentation/components/create-appointment-dialog/create-appointment-dialog.component";
import { LocalStorageService } from "../../../../../shared/services/local-storage.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";

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
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
  ],
})
export class AppointmentsComponent {
  private readonly appointmentService = inject(AppointmentService);
  private readonly dialog = inject(MatDialog);
  private readonly localStorageService = inject(LocalStorageService);
  private readonly snackbarService = inject(SnackbarService);
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

  canCreate: boolean = true;

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

  createAppointment() {
    const userRole = this.localStorageService.getUserRole();
    const personId = this.localStorageService.getUserData()?.person?.id || 0;

    const dialogRef = this.dialog.open(CreateAppointmentDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      data: {
        idDentist: userRole === RoleEnum.DENTIST ? personId : undefined,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success) {
        this.snackbarService.openSnackbar(
          "Turno creado exitosamente",
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Success,
        );
        this._loadData();
      }
    });
  }
}
