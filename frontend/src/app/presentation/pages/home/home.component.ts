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
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { Router } from "@angular/router";
import { AuthService } from "../../../services/auth.service";
import { DentistService } from "../../../services/dentist.service";
import { UserDataInterface } from "../../../domain/interfaces/user-data.interface";
import { DentistAvailabilityResponseInterface } from "../../../domain/interfaces/dentist.interface";
import { Subject, takeUntil } from "rxjs";
import { DayEnum } from "../../../utils/enums/day.enum";

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
  ],
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly authService = inject(AuthService);
  private readonly dentistService = inject(DentistService);
  private readonly router = inject(Router);

  userData = signal<UserDataInterface | null>(null);
  dentistAvailability = signal<DentistAvailabilityResponseInterface | null>(
    null
  );
  isLoadingAvailability = signal<boolean>(false);
  currentTime = signal<string>("");
  currentDate = signal<string>("");

  greeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 12) return "Buenos días";
    if (hour < 20) return "Buenas tardes";
    return "Buenas noches";
  });

  constructor() {
    // Cargar disponibilidad si es dentista
    // effect(() => {
    //   if (this.userData() && this.isDentist() && this.userData()?.person?.id) {
    //     this.isLoadingAvailability.set(true);
    //     this.dentistService
    //       .getAvailability(this.userData()?.person?.id!)
    //       .pipe(takeUntil(this._destroy$))
    //       .subscribe({
    //         next: (response) => {
    //           this.dentistAvailability.set(response.data);
    //           this.isLoadingAvailability.set(false);
    //         },
    //         error: () => {
    //           this.isLoadingAvailability.set(false);
    //         },
    //       });
    //   }
    // });
  }

  ngOnInit(): void {
    this.userData.set(this.authService.getUserData());
    this.updateTime();
    // Actualizar la hora cada minuto
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
      })
    );
    this.currentDate.set(
      now.toLocaleDateString("es-AR", {
        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric",
      })
    );
  }

  isDentist(): boolean {
    return (
      this.userData()?.roles?.some((role) => role.name === "DENTIST") || false
    );
  }

  getWeeklyDays() {
    return (
      this.dentistAvailability()?.days?.filter(
        (day) => day.recurrence === "WEEKLY"
      ) || []
    );
  }

  getSpecificDays() {
    return (
      this.dentistAvailability()?.days?.filter(
        (day) => !day.recurrence || day.recurrence === "NONE"
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

  goToCalendar(): void {
    this.router.navigate(["/calendar"]);
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
