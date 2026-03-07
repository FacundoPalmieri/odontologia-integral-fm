import {
  Component,
  computed,
  effect,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { Router } from "@angular/router";
import { Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { CardIconTitleComponent } from "../../../../../shared/components/card-icon-title/card-icon-title.component";
import { UserDataInterface } from "../../../../auth/data/interfaces/auth.interface";
import { DentistAvailabilityService } from "../../../../dentist-availability/services/dentist-availability.service";
import { DayEnum } from "../../../../../shared/utils/enums/day.enum";
import { CalendarService } from "../../../../calendar/services/calendar.service";
import { SlotStatusEnum } from "../../../../calendar/utils/enums/slot-status.enum";
import { LocalStorageService } from "../../../../../shared/services/local-storage.service";
import { DentistAvailabilityResponseInterface } from "../../../../dentist-availability/data/interfaces/dentist-availability.interface";
import {
  CalendarDayInterface,
  CalendarSlotInterface,
} from "../../../../calendar/data/interfaces/calendar.interface";

@Component({
  selector: "app-home",
  templateUrl: "./home.component.html",
  styleUrl: "./home.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    MatCardModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    CardIconTitleComponent,
  ],
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly localStorageService = inject(LocalStorageService);
  private readonly dentistAvailabilityService = inject(
    DentistAvailabilityService,
  );
  private readonly calendarService = inject(CalendarService);
  private readonly router = inject(Router);

  userData = signal<UserDataInterface | null>(null);
  dentistAvailability = signal<DentistAvailabilityResponseInterface | null>(
    null,
  );
  isLoadingAvailability = signal<boolean>(false);
  currentTime = signal<string>("");
  currentDate = signal<string>("");

  todayCalendarData = signal<CalendarDayInterface | null>(null);
  isLoadingTodayAppointments = signal<boolean>(false);

  todayAppointments = computed(() => {
    const slots = this.todayCalendarData()?.slots || [];
    return slots.filter(
      (slot) =>
        slot.status !== SlotStatusEnum.FREE &&
        slot.status !== SlotStatusEnum.NOT_AVAILABLE &&
        slot.status !== SlotStatusEnum.LOCKED,
    );
  });

  hasAvailability = computed(() => {
    const availability = this.dentistAvailability();
    return availability && availability.days && availability.days.length > 0;
  });

  nextAppointment = computed(() => {
    const now = new Date();
    const currentTimeString = `${String(now.getHours()).padStart(
      2,
      "0",
    )}:${String(now.getMinutes()).padStart(2, "0")}:00`;

    const appointments = this.todayAppointments();
    const upcoming = appointments.filter(
      (slot) => slot.startTime >= currentTimeString,
    );

    return upcoming.length > 0 ? upcoming[0] : null;
  });

  greeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 12) return "Buenos días";
    if (hour < 20) return "Buenas tardes";
    return "Buenas noches";
  });

  constructor() {
    effect(() => {
      if (
        this.userData() &&
        this.shouldShowDentistInfo() &&
        this.userData()?.person?.id
      ) {
        this.isLoadingAvailability.set(true);
        this.dentistAvailabilityService
          .get(this.userData()?.person?.id!)
          .pipe(takeUntil(this._destroy$))
          .subscribe({
            next: (response) => {
              this.dentistAvailability.set(response.data);
              this.isLoadingAvailability.set(false);
            },
            error: () => {
              this.isLoadingAvailability.set(false);
            },
          });
      }
    });

    effect(() => {
      if (
        this.userData() &&
        this.shouldShowDentistInfo() &&
        this.userData()?.person?.id &&
        this.hasAvailability()
      ) {
        this.loadTodayAppointments();
      }
    });
  }

  ngOnInit(): void {
    this.userData.set(this.localStorageService.getUserData());
    this.updateTime();
    setInterval(() => this.updateTime(), 60000);
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  updateTime(): void {
    const now = new Date();
    this.currentTime.set(
      now.toLocaleTimeString("es-AR", {
        hour: "2-digit",
        minute: "2-digit",
      }),
    );
    this.currentDate.set(
      now.toLocaleDateString("es-AR", {
        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric",
      }),
    );
  }

  shouldShowDentistInfo(): boolean {
    return this.localStorageService.isDentist();
  }

  getWeeklyDays() {
    return (
      this.dentistAvailability()?.days?.filter(
        (day) => day.recurrence === "WEEKLY",
      ) || []
    );
  }

  getSpecificDays() {
    return (
      this.dentistAvailability()?.days?.filter(
        (day) => !day.recurrence || day.recurrence === "NONE",
      ) || []
    );
  }

  getDayLabel(day: DayEnum | null | undefined): string {
    if (!day) return "-";
    const dayLabels: Record<DayEnum, string> = {
      [DayEnum.MONDAY]: "Lunes",
      [DayEnum.TUESDAY]: "Martes",
      [DayEnum.WEDNESDAY]: "Miércoles",
      [DayEnum.THURSDAY]: "Jueves",
      [DayEnum.FRIDAY]: "Viernes",
      [DayEnum.SATURDAY]: "Sábado",
      [DayEnum.SUNDAY]: "Domingo",
    };
    return dayLabels[day] || day;
  }

  formatTime(time: { hour: number; minute: number }): string {
    const hour = time.hour.toString().padStart(2, "0");
    const minute = time.minute.toString().padStart(2, "0");
    return `${hour}:${minute}`;
  }

  formatDate(date: string | Date | null): string {
    if (!date) return "-";
    const dateObj = typeof date === "string" ? new Date(date) : date;
    return dateObj.toLocaleDateString("es-AR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  }

  loadTodayAppointments(): void {
    const personId = this.userData()?.person?.id;
    if (!personId) return;

    this.isLoadingTodayAppointments.set(true);
    const today = new Date();

    this.calendarService
      .getDay(personId, today)
      .pipe(takeUntil(this._destroy$))
      .subscribe({
        next: (response) => {
          this.todayCalendarData.set(response.data);
          this.isLoadingTodayAppointments.set(false);
        },
        error: () => {
          this.isLoadingTodayAppointments.set(false);
        },
      });
  }

  getSlotPatientName(slot: CalendarSlotInterface): string {
    if (slot.appointment?.patientName) {
      return slot.appointment.patientName;
    }
    if (slot.calendarLock?.lockType) {
      return slot.calendarLock.lockType;
    }
    return "-";
  }

  goToCalendar(): void {
    this.router.navigate(["/calendar"]);
  }

  goToAppointments(): void {
    this.router.navigate(["/appointments"]);
  }

  goToAvailability(): void {
    if (this.userData()?.person?.id) {
      this.router.navigate([
        "/dentist-availability",
        this.userData()?.person?.id,
      ]);
    }
  }
}
