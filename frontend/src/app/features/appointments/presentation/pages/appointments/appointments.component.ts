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
import { CalendarService } from "../../../../calendar/services/calendar.service";
import { forkJoin } from "rxjs";

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
  private readonly dialog = inject(MatDialog);
  private readonly localStorageService = inject(LocalStorageService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly dentistService = inject(DentistService);
  private readonly calendarService = inject(CalendarService);

  appointments = signal<any[]>([]);
  allTodayAppointments = signal<any[]>([]);
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

  readonly scheduledCount = computed(() => 
    this.allTodayAppointments().filter(a => a.status === 'Agendado').length
  );
  readonly inProgressCount = computed(() => 
    this.allTodayAppointments().filter(a => a.status === 'En consulta').length
  );
  readonly pendingPaymentCount = computed(() => 
    this.allTodayAppointments().filter(a => a.status === 'Pendiente de pago').length
  );
  readonly finalizedCount = computed(() => 
    this.allTodayAppointments().filter(a => a.status === 'Finalizada').length
  );
  readonly canceledCount = computed(() => 
    this.allTodayAppointments().filter(a => a.status === 'Cancelada').length
  );
  readonly waitingCount = computed(() => 
    this.allTodayAppointments().filter(a => a.status === 'En espera').length
  );

  constructor() {
    this._loadData();
    this._loadDentists();
    effect(() => {
      localStorage.setItem("appointmentsViewMode", this.viewMode());
    });
  }

  toggleFilter(status: string) {
    if (this.activeFilter() === status) {
      this.activeFilter.set(null);
      this.appointments.set(this.allTodayAppointments());
    } else {
      this.activeFilter.set(status);
      this.appointments.set(
        this.allTodayAppointments().filter((a) => a.status === status)
      );
    }
  }

  toggleScheduled() {
    this.toggleFilter("Agendado");
  }

  toggleWaiting() {
    this.toggleFilter("En espera");
  }

  toggleInProgress() {
    this.toggleFilter("En consulta");
  }

  togglePendingPayment() {
    this.toggleFilter("Pendiente de pago");
  }

  toggleFinalized() {
    this.toggleFilter("Finalizada");
  }

  toggleCanceled() {
    this.toggleFilter("Cancelada");
  }

  protected _loadData() {
    const userRole = this.localStorageService.getUserRole();
    const personId = this.localStorageService.getUserData()?.person?.id || 0;
    const today = new Date();

    if (userRole === RoleEnum.DENTIST) {
      this.calendarService.getDay(personId, today).subscribe((response) => {
        const slots = response.data?.slots || [];
        const mapped = this._mapSlotsToAppointments(slots);
        this.allTodayAppointments.set(mapped);
        this.appointments.set(mapped);
      });
    } else {
      this.dentistService.getAll().subscribe((response) => {
        const dentists = response.data || [];
        if (dentists.length === 0) {
          this.allTodayAppointments.set([]);
          this.appointments.set([]);
          return;
        }

        const requests = dentists.map((d) =>
          this.calendarService.getDay(d.person.id, today),
        );

        forkJoin(requests).subscribe((responses) => {
          const allSlots = responses.flatMap((res) => res.data?.slots || []);
          const mapped = this._mapSlotsToAppointments(allSlots);
          this.allTodayAppointments.set(mapped);
          this.appointments.set(mapped);
        });
      });
    }
  }

  private _mapSlotsToAppointments(slots: any[]): any[] {
    return slots
      .filter((slot) => slot.status === "RESERVED" && slot.appointment)
      .map((slot) => {
        const appointment = slot.appointment;
        const [startH, startM] = slot.startTime.split(":").map(Number);
        const [endH, endM] = slot.endTime.split(":").map(Number);
        const duration = endH * 60 + endM - (startH * 60 + startM);

        const nameParts = appointment.patientName
          ? appointment.patientName.split(",").map((s: string) => s.trim())
          : [];
        const lastName = nameParts[0] || appointment.patientName || "N/A";
        const firstName = nameParts[1] || "";

        return {
          id: appointment.id,
          firstName,
          lastName,
          appointmentDateTime: appointment.appointmentDateTime,
          duration,
          professional: appointment.dentistName,
          status: "Agendado",
        };
      })
      .sort((a, b) => {
        return (
          new Date(a.appointmentDateTime).getTime() -
          new Date(b.appointmentDateTime).getTime()
        );
      });
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
