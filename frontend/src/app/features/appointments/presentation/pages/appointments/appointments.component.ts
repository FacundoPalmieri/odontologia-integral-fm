import {
  Component,
  inject,
  signal,
  effect,
  computed,
  ChangeDetectionStrategy,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { ReactiveFormsModule, FormControl } from "@angular/forms";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent, ToolbarSelectOption } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { AppointmentService } from "../../../services/appointment.service";
import { RoleEnum } from "../../../../../shared/utils/enums/role.enum";
import { MatDialogModule, MatDialog } from "@angular/material/dialog";
import { CreateAppointmentDialogComponent } from "../../../../calendar/presentation/components/create-appointment-dialog/create-appointment-dialog.component";
import { LocalStorageService } from "../../../../../shared/services/local-storage.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { DentistService } from "../../../../calendar/services/dentist.service";
import { DentistDto } from "../../../../calendar/data/dtos/dentist.dto";
import { AppointmentsTableComponent } from "../../components/appointments-table/appointments-table.component";
import { AppointmentsCardsComponent } from "../../components/appointments-cards/appointments-cards.component";

@Component({
  selector: "app-appointments",
  templateUrl: "./appointments.component.html",
  styleUrl: "./appointments.component.scss",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    AppointmentsTableComponent,
    AppointmentsCardsComponent,
  ],
})
export class AppointmentsComponent {
  private readonly appointmentService = inject(AppointmentService);
  private readonly dialog = inject(MatDialog);
  private readonly localStorageService = inject(LocalStorageService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly dentistService = inject(DentistService);

  appointments = signal<any[]>([]);
  canCreate = true;
  activeFilter = signal<string | null>(null);

  // Filter card
  patientSearchControl = new FormControl<string>("", { nonNullable: true });
  professionalsControl = new FormControl<number[]>([], { nonNullable: true });
  viewMode = signal<"table" | "cards">(
    (localStorage.getItem("appointmentsViewMode") as "table" | "cards") ||
      "table",
  );
  dentists = signal<DentistDto[]>([]);

  readonly dentistsOptions = computed<ToolbarSelectOption[]>(() => {
    return this.dentists().map((d) => ({
      id: d.person.id,
      label: `${d.person.firstName} ${d.person.lastName}`,
    }));
  });

  constructor() {
    this._loadData();
    this._loadDentists();
    effect(() => {
      localStorage.setItem("appointmentsViewMode", this.viewMode());
    });
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
  }

  private _loadDentists() {
    this.dentistService.getAll().subscribe((response) => {
      this.dentists.set(response.data ?? []);
    });
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
