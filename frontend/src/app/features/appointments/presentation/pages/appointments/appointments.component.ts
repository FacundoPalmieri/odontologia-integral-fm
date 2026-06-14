import {
  Component,
  inject,
  signal,
  effect,
  computed,
  ChangeDetectionStrategy,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { WebsocketService } from "../../../../../core/services/websocket.service";
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
import { ConsultationService } from "../../../services/consultation.service";
import { forkJoin, Observable } from "rxjs";

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
  private readonly websocketService = inject(WebsocketService);
  private readonly consultationService = inject(ConsultationService);
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

    effect(() => {
      localStorage.setItem("appointmentsViewMode", this.viewMode());
    });

    this.websocketService.consultationUpdates$
      .pipe(takeUntilDestroyed())
      .subscribe((payload) => {
        console.log("[AppointmentsComponent] Consultation update received:", payload);
        const type = payload?.type;
        const consultations = payload?.data;

        // If the socket payload contains the full array of active consultations
        if (Array.isArray(consultations)) {
          const consultationsMapped = consultations.map((c: any) => {
            return {
              id: c.id,
              firstName: c.patientName,
              lastName: "",
              appointmentDateTime: null,
              duration: null,
              professional: c.dentistName,
              status: this.mapSocketStatusToLocalStatus(c.consultationStatus),
            };
          });

          // Filter out the old consultations from allTodayAppointments, keeping only the calendar appointments
          const calendarAppointments = this.allTodayAppointments().filter(a => a.status === 'Agendado');

          // Combine calendar appointments with new consultations
          const combined = [...calendarAppointments, ...consultationsMapped];
          const localStatus = type ? this.mapSocketStatusToLocalStatus(type) : null;
          this.applyConsultationsData(combined, localStatus);
        } else {
          // Fallback if data is not an array (fetch via HTTP)
          const localStatus = type ? this.mapSocketStatusToLocalStatus(type) : undefined;
          this._loadData(localStatus);
        }
      });
  }

  private mapSocketStatusToLocalStatus(socketStatusOrType: string): string {
    const statusMap: { [key: string]: string } = {
      // Map by type (event)
      "PATIENT_RECEIVED": "En espera",
      "ATTENTION_STARTED": "En consulta",
      "PAYMENT_REGISTERED": "Pendiente de pago",
      "ATTENTION_FINISHED": "Finalizada",
      // Map by status string
      "Sala de Espera": "En espera",
      "En Atención": "En consulta",
      "Pendiente de Pago": "Pendiente de pago",
      "Finalizada": "Finalizada",
    };
    return statusMap[socketStatusOrType] || socketStatusOrType;
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

  private applyConsultationsData(mapped: any[], targetFilter?: string | null) {
    this.allTodayAppointments.set(mapped);
    
    const filter = targetFilter !== undefined ? targetFilter : this.activeFilter();
    this.activeFilter.set(filter);
    
    if (filter) {
      this.appointments.set(mapped.filter((a) => a.status === filter));
    } else {
      this.appointments.set(mapped);
    }
  }

  protected _loadData(targetFilter?: string | null) {
    const userRole = this.localStorageService.getUserRole();
    const personId = this.localStorageService.getUserData()?.person?.id || 0;
    const today = new Date();

    const dentists$: Observable<DentistDto[]> = this.dentists().length > 0
      ? new Observable<DentistDto[]>(sub => { sub.next(this.dentists()); sub.complete(); })
      : new Observable<DentistDto[]>(sub => {
          this.dentistService.getAll().subscribe({
            next: (response) => {
              const list = response.data || [];
              this.dentists.set(list);
              sub.next(list);
              sub.complete();
            },
            error: (err) => sub.error(err)
          });
        });

    dentists$.subscribe({
      next: (dentistsList) => {
        let calendarObs$: Observable<any>;
        if (userRole === RoleEnum.DENTIST) {
          calendarObs$ = this.calendarService.getDay(personId, today);
        } else {
          if (dentistsList.length === 0) {
            calendarObs$ = new Observable<any>(sub => { sub.next({ data: { slots: [] } }); sub.complete(); });
          } else {
            const requests = dentistsList.map((d) =>
              this.calendarService.getDay(d.person.id, today)
            );
            calendarObs$ = new Observable<any>(sub => {
              forkJoin(requests).subscribe({
                next: (responses) => {
                  const allSlots = responses.flatMap((res) => res.data?.slots || []);
                  sub.next({ data: { slots: allSlots } });
                  sub.complete();
                },
                error: (err) => sub.error(err)
              });
            });
          }
        }

        forkJoin({
          consultations: this.consultationService.getConsultations(),
          calendar: calendarObs$
        }).subscribe({
          next: (result) => {
            console.log("[AppointmentsComponent] Data fetched successfully:", result);
            
            // Map calendar slots
            const slots = result.calendar?.data?.slots || [];
            const calendarMapped = this._mapSlotsToAppointments(slots);

            // Map active consultations
            const consultations = result.consultations?.data || [];
            const consultationsMapped = consultations.map((c: any) => {
              return {
                id: c.id,
                firstName: c.patientName,
                lastName: "",
                appointmentDateTime: null,
                duration: null,
                professional: c.dentistName,
                status: this.mapSocketStatusToLocalStatus(c.consultationStatus),
              };
            });

            // Combine both lists (union)
            const combined = [...calendarMapped, ...consultationsMapped];
            this.applyConsultationsData(combined, targetFilter);
          },
          error: (err) => {
            console.error("[AppointmentsComponent] Error loading today's data:", err);
            this.allTodayAppointments.set([]);
            this.appointments.set([]);
          }
        });
      },
      error: (err) => {
        console.error("[AppointmentsComponent] Error fetching dentists:", err);
      }
    });
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

        const startTime = slot.startTime.split(":").slice(0, 2).join(":");

        return {
          id: appointment.id,
          firstName,
          lastName,
          appointmentDateTime: appointment.appointmentDateTime,
          startTime,
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
