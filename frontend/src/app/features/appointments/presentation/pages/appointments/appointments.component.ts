import {
  Component,
  effect,
  inject,
  signal,
  ViewChild,
  AfterViewInit,
} from "@angular/core";
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
import { MatMenuModule } from "@angular/material/menu";
import { MatDividerModule } from "@angular/material/divider";
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
    MatMenuModule,
    MatDividerModule,
  ],
})
export class AppointmentsComponent implements AfterViewInit {
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
  ];

  canCreate: boolean = true;
  activeFilter = signal<string | null>(null);

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

  ngAfterViewInit() {
    this.appointmentsDataSource.paginator = this.paginator;
    this.appointmentsDataSource.sort = this.sort;
  }

  toggleFilter(filter: string, fetchFn: () => any[]) {
    if (this.activeFilter() === filter) {
      this.activeFilter.set(null);
      this._loadData();
    } else {
      this.activeFilter.set(filter);
      this.appointments.set(fetchFn());
    }
  }

  toggleWaiting() {
    this.toggleFilter("waiting", () => this.appointmentService.getWaiting());
  }

  toggleInProgress() {
    this.toggleFilter("in-progress", () =>
      this.appointmentService.getInProgress(),
    );
  }

  togglePendingPayment() {
    this.toggleFilter("pending-payment", () =>
      this.appointmentService.getPendingPayment(),
    );
  }

  toggleFinalized() {
    this.toggleFilter("finalized", () =>
      this.appointmentService.getFinalized(),
    );
  }

  toggleCanceled() {
    this.toggleFilter("canceled", () => this.appointmentService.getCanceled());
  }

  private _loadData() {
    this.appointments.set(this.appointmentService.getAll());
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
