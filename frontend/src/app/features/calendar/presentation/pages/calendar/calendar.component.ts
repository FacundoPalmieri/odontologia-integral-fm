import {
  Component,
  inject,
  OnInit,
  AfterViewInit,
  ElementRef,
  ViewChild,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";
import { MatChipsModule } from "@angular/material/chips";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatTabsModule } from "@angular/material/tabs";
import { MatMenuModule } from "@angular/material/menu";
import { MatDialogModule, MatDialog } from "@angular/material/dialog";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatBadgeModule } from "@angular/material/badge";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatRadioModule } from "@angular/material/radio";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { CreateAppointmentDialogComponent } from "../../components/create-appointment-dialog/create-appointment-dialog.component";
import { CreateCalendarLockDialogComponent } from "../../components/create-calendar-lock-dialog/create-calendar-lock-dialog.component";
import { Router } from "@angular/router";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { forkJoin } from "rxjs";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { LoaderService } from "../../../../../core/services/loader.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { DentistService } from "../../../services/dentist.service";
import { AppointmentService } from "../../../../appointments/services/appointment.service";
import { CalendarService } from "../../../services/calendar.service";
import { RoleEnum } from "../../../../../shared/utils/enums/role.enum";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { AppointmentsConflictDialogComponent } from "../../../../../shared/components/appointments-conflict-dialog/appointments-conflict-dialog.component";
import { CancelAllAppointmentsDialog } from "../../components/cancel-all-appointments-dialog/cancel-all-appointments-dialog.component";
import { RequestSourceEnum } from "../../../../../shared/utils/enums/request-source.enum";
import { PersonInterface } from "../../../../../shared/interfaces/person.interface";
import { HolidayDetailDialogComponent } from "../../components/holiday-detail-dialog/holiday-detail-dialog.component";
import { CalendarLockDetailDialogComponent } from "../../components/calendar-lock-detail-dialog/calendar-lock-detail-dialog.component";
import { UnlockCalendarDialog } from "../../components/unlock-calendar-dialog/unlock-calendar-dialog.component";
import { CalendarLockService } from "../../../services/calendar-lock.service";
import { AppointmentDetailDialogComponent } from "../../components/appointment-detail-dialog/appointment-detail-dialog.component";
import { CalendarMonthDayStatusEnum } from "../../../utils/enums/calendar-month-day-status.enum";
import { SlotStatusEnum } from "../../../utils/enums/slot-status.enum";
import { PatientInterface } from "../../../../patients/data/interfaces/patient.interface";
import { LocalStorageService } from "../../../../../shared/services/local-storage.service";
import { DentistDto } from "../../../data/dtos/dentist.dto";
import {
  CalendarDayInterface,
  CalendarMonthDayInterface,
  CalendarMonthInterface,
  CalendarWeekInterface,
  DentistLockMonthInterface,
} from "../../../data/interfaces/calendar.interface";
import { CalendarLockDayInterface } from "../../../data/interfaces/calendar-lock.interface";
import { AppointmentInterface } from "../../../../appointments/data/interfaces/appointment.inteface";

export type CalendarView = "day" | "week" | "month";

export interface SpecialtyGroup {
  specialtyName: string;
  dentists: DentistDto[];
}

@Component({
  selector: "app-calendar",
  templateUrl: "./calendar.component.html",
  styleUrls: ["./calendar.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatToolbarModule,
    MatTabsModule,
    MatMenuModule,
    MatDialogModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatRadioModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatBadgeModule,
  ],
})
export class CalendarComponent implements OnInit, AfterViewInit {
  private readonly loaderService = inject(LoaderService);
  private readonly router = inject(Router);
  private readonly calendarService = inject(CalendarService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly calendarLockService = inject(CalendarLockService);
  private readonly dentistService = inject(DentistService);
  private readonly appointmentService = inject(AppointmentService);
  readonly localStorageService = inject(LocalStorageService);
  dialog = inject(MatDialog);
  loading$ = this.loaderService.loading$;

  personId: number = 0;

  @ViewChild("timeColumn", { static: false }) timeColumn!: ElementRef;
  @ViewChild("eventsColumn", { static: false }) eventsColumn!: ElementRef;

  private scrollHandler: (() => void) | null = null;

  isLoadingCalendar = signal<boolean>(false);
  calendarMonthData = signal<CalendarMonthInterface | null>(null);
  calendarWeekData = signal<CalendarWeekInterface | null>(null);
  calendarDayData = signal<CalendarDayInterface | null>(null);

  appointmentConflicts = signal<any[]>([]);
  hasConflicts = signal<boolean>(false);

  private monthCache = new Map<string, CalendarMonthInterface>();

  private currentDayKey: string = "";

  currentDate = new Date();
  selectedDate = new Date();
  currentView: CalendarView = "month";

  myCalendarsExpanded = true;
  otherCalendarsExpanded = true;
  sidebarCollapsed = false;

  sidebarDentists = signal<DentistDto[]>([]);
  selectedSidebarDentistId = signal<number | null>(null);

  specialtyGroups = signal<SpecialtyGroup[]>([]);
  selectedDentist = signal<DentistDto | null>(null);
  isLoadingDentists = signal<boolean>(false);
  showDentistSelection = signal<boolean>(false);

  workStartHour = 0;
  workEndHour = 24;

  timeSlots: string[] = [];

  weekDays = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];

  months = [
    "Enero",
    "Febrero",
    "Marzo",
    "Abril",
    "Mayo",
    "Junio",
    "Julio",
    "Agosto",
    "Septiembre",
    "Octubre",
    "Noviembre",
    "Diciembre",
  ];

  constructor() {
    this.generateTimeSlots();
  }

  ngOnInit() {
    this.personId = this.localStorageService.getUserData()?.person.id || 0;
    this.updateSelectedDate();

    if (this.localStorageService.isSecretary()) {
      this.sidebarCollapsed = true;
      this.showDentistSelection.set(true);
      this.loadDentists();
      this.loadSidebarDentists();
    } else if (this.localStorageService.isAdministrator()) {
      this.loadSidebarDentists();
      this.selectedSidebarDentistId.set(this.personId);

      this.loadConflicts();

      if (this.currentView === "month") {
        this.loadMonthView();
      } else if (this.currentView === "week") {
        this.loadWeekView();
      } else if (this.currentView === "day") {
        this.loadDayView();
      }
    } else {
      this.selectedSidebarDentistId.set(this.personId);

      this.loadConflicts();

      if (this.currentView === "month") {
        this.loadMonthView();
      }
    }
  }

  ngAfterViewInit() {
    this.setupScrollSync();
    this.startTimeUpdate();
  }

  /**
   * Load month view data from backend
   * Loads current month + previous month + next month to cover all visible days
   * Uses cache to avoid redundant API calls
   */
  loadMonthView() {
    if (!this.personId) {
      console.error("No person ID available");
      return;
    }

    this.isLoadingCalendar.set(true);
    const year = this.selectedDate.getFullYear();
    const month = this.selectedDate.getMonth() + 1;

    const prevDate = new Date(year, month - 2, 1);
    const nextDate = new Date(year, month, 1);

    const prevYear = prevDate.getFullYear();
    const prevMonth = prevDate.getMonth() + 1;
    const nextYear = nextDate.getFullYear();
    const nextMonth = nextDate.getMonth() + 1;

    const prevKey = `${prevYear}-${String(prevMonth).padStart(2, "0")}`;
    const currentKey = `${year}-${String(month).padStart(2, "0")}`;
    const nextKey = `${nextYear}-${String(nextMonth).padStart(2, "0")}`;

    const requests: { [key: string]: any } = {};

    if (!this.monthCache.has(prevKey)) {
      requests["prev"] = this.calendarService.getMonth(
        this.personId,
        prevYear,
        prevMonth,
      );
    }
    if (!this.monthCache.has(currentKey)) {
      requests["current"] = this.calendarService.getMonth(
        this.personId,
        year,
        month,
      );
    }
    if (!this.monthCache.has(nextKey)) {
      requests["next"] = this.calendarService.getMonth(
        this.personId,
        nextYear,
        nextMonth,
      );
    }

    if (Object.keys(requests).length === 0) {
      this.combineMonthsFromCache(prevKey, currentKey, nextKey);
      this.isLoadingCalendar.set(false);
      return;
    }

    forkJoin(requests).subscribe({
      next: (responses: any) => {
        if (responses["prev"]?.success && responses["prev"].data) {
          this.monthCache.set(prevKey, responses["prev"].data);
        }
        if (responses["current"]?.success && responses["current"].data) {
          this.monthCache.set(currentKey, responses["current"].data);
        }
        if (responses["next"]?.success && responses["next"].data) {
          this.monthCache.set(nextKey, responses["next"].data);
        }

        this.combineMonthsFromCache(prevKey, currentKey, nextKey);
        this.isLoadingCalendar.set(false);
      },
      error: (error) => {
        console.error("❌ Error al cargar mes:", error);
        this.isLoadingCalendar.set(false);
        this.calendarMonthData.set(null);
      },
    });
  }

  /**
   * Combine months from cache
   */
  private combineMonthsFromCache(
    prevKey: string,
    currentKey: string,
    nextKey: string,
  ) {
    const allDays: CalendarMonthDayInterface[] = [];

    const prevData = this.monthCache.get(prevKey);
    const currentData = this.monthCache.get(currentKey);
    const nextData = this.monthCache.get(nextKey);

    if (prevData?.days) {
      allDays.push(...prevData.days);
    }
    if (currentData?.days) {
      allDays.push(...currentData.days);
    }
    if (nextData?.days) {
      allDays.push(...nextData.days);
    }

    if (currentData) {
      this.calendarMonthData.set({
        ...currentData,
        days: allDays,
      });
    } else {
      this.calendarMonthData.set(null);
    }
  }

  /**
   * Load day view data from backend
   * Logs the response and updates calendarDayData signal
   */
  loadDayView() {
    if (!this.personId) {
      return;
    }

    this.isLoadingCalendar.set(true);
    const dateToLoad = new Date(this.selectedDate);

    this.calendarService.getDay(this.personId, dateToLoad).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.calendarDayData.set(response.data);
        } else {
          this.calendarDayData.set(null);
        }

        this.isLoadingCalendar.set(false);
      },
    });
  }

  /**
   * Load week view data from backend
   * Updates calendarWeekData signal
   */
  loadWeekView() {
    if (!this.personId) {
      console.warn("⚠️ No personId available");
      return;
    }

    this.isLoadingCalendar.set(true);
    const dateToLoad = new Date(this.selectedDate);

    this.calendarService.getWeek(this.personId, dateToLoad).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.calendarWeekData.set(response.data);
        } else {
          this.calendarWeekData.set(null);
        }

        this.isLoadingCalendar.set(false);
      },
      error: (error) => {
        console.error("❌ Error loading week data:", error);
        this.isLoadingCalendar.set(false);
      },
    });
  }

  generateTimeSlots() {
    for (let hour = this.workStartHour; hour < this.workEndHour; hour++) {
      const timeString = `${hour.toString().padStart(2, "0")}:00`;
      this.timeSlots.push(timeString);
    }
  }

  setView(view: CalendarView) {
    this.currentView = view;

    if (view === "month") {
      this.monthCache.clear();
      this.loadMonthView();
    } else if (view === "week") {
      this.loadWeekView();
    } else if (view === "day") {
      this.loadDayView();
    }

    setTimeout(() => {
      this.setupScrollSync();
    }, 100);
  }

  navigateDate(direction: "prev" | "next") {
    const newDate = new Date(this.selectedDate);

    switch (this.currentView) {
      case "day":
        newDate.setDate(newDate.getDate() + (direction === "next" ? 1 : -1));
        break;
      case "week":
        newDate.setDate(newDate.getDate() + (direction === "next" ? 7 : -7));
        break;
      case "month":
        newDate.setMonth(newDate.getMonth() + (direction === "next" ? 1 : -1));
        break;
    }

    this.selectedDate = newDate;
    this.updateSelectedDate();

    if (this.currentView === "month") {
      this.loadMonthView();
    } else if (this.currentView === "week") {
      this.loadWeekView();
    } else if (this.currentView === "day") {
      this.loadDayView();
    }
  }

  goToToday() {
    this.selectedDate = new Date();
    this.updateSelectedDate();

    if (this.currentView === "month") {
      this.loadMonthView();
    } else if (this.currentView === "week") {
      this.loadWeekView();
    } else if (this.currentView === "day") {
      this.loadDayView();
    }
  }

  updateSelectedDate() {
    this.currentDate = new Date();
  }

  getWeekDates(): Date[] {
    const startOfWeek = new Date(this.selectedDate);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day;
    startOfWeek.setDate(diff);

    const weekDates: Date[] = [];
    for (let i = 0; i < 7; i++) {
      const date = new Date(startOfWeek);
      date.setDate(startOfWeek.getDate() + i);
      weekDates.push(date);
    }
    return weekDates;
  }

  getMonthDates(): Date[] {
    const year = this.selectedDate.getFullYear();
    const month = this.selectedDate.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());

    const dates: Date[] = [];
    const current = new Date(startDate);

    while (
      current <= lastDay ||
      (current > lastDay && current.getDay() !== 0)
    ) {
      dates.push(new Date(current));
      current.setDate(current.getDate() + 1);
    }

    return dates;
  }

  getMonthWeeks(): Date[][] {
    const dates = this.getMonthDates();
    const weeks: Date[][] = [];

    for (let i = 0; i < dates.length; i += 7) {
      weeks.push(dates.slice(i, i + 7));
    }

    return weeks;
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  }

  isCurrentMonth(date: Date): boolean {
    return date.getMonth() === this.selectedDate.getMonth();
  }

  /**
   * Check if a day is NOT_AVAILABLE (for styling in monthly view)
   * Los días con feriado NO se marcan como deshabilitados
   */
  isNotAvailableDay(date: Date): boolean {
    const dayData = this.getDayFromBackend(date);

    if (dayData?.holiday) {
      return false;
    }

    return (dayData?.calendarDayStatus?.key as string) === "NOT_AVAILABLE";
  }

  selectDate(date: Date) {
    this.selectedDate = date;
  }

  /**
   * Get day data from backend calendar month data
   */
  getDayFromBackend(date: Date): CalendarMonthDayInterface | null {
    const calendarData = this.calendarMonthData();

    if (!calendarData || !calendarData.days) {
      return null;
    }

    const dateKey = `${date.getFullYear()}-${String(
      date.getMonth() + 1,
    ).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

    return calendarData.days.find((day) => day.day === dateKey) || null;
  }

  /**
   * Get the color for a specific day from backend data
   * Returns the color from calendarDayStatus
   */
  getDayColor(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    return dayData?.calendarDayStatus?.color || null;
  }

  /**
   * Get the description for a specific day (for holidays, full days, locked days, and free days)
   * NOT_AVAILABLE days are excluded from showing badges in monthly view
   */
  getDayDescription(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    // Retornar la descripción del calendarDayStatus si existe
    return dayData?.calendarDayStatus?.description || null;
  }

  /**
   * Get the icon for a specific day based on its status
   */
  getDayIcon(date: Date): string {
    const dayData = this.getDayFromBackend(date);
    const statusKey = dayData?.calendarDayStatus?.key as string;

    if (statusKey === CalendarMonthDayStatusEnum.FULL) {
      return "⛔";
    }

    if (statusKey === CalendarMonthDayStatusEnum.LOCKED) {
      return "🔒";
    }

    if (statusKey === CalendarMonthDayStatusEnum.FREE) {
      return "✅";
    }

    return "📅";
  }

  /**
   * Get the holiday description for a specific day
   */
  getHolidayDescription(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    // Retornar la descripción del holiday si existe
    return dayData?.holiday?.description || null;
  }

  /**
   * Get the holiday color for a specific day
   */
  getHolidayColor(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    return dayData?.holiday?.color || "#48925f";
  }

  /**
   * Open holiday detail dialog
   */
  openHolidayDetail(date: Date, event: Event): void {
    event.stopPropagation();

    const dayData = this.getDayFromBackend(date);
    if (!dayData?.holiday) {
      return;
    }

    const isWorking =
      dayData.calendarDayStatus?.key === CalendarMonthDayStatusEnum.FREE;

    const dialogRef = this.dialog.open(HolidayDetailDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      data: {
        holiday: dayData.holiday,
        date: date,
        dentistHolidayId: dayData.dentistHolidayId,
        isWorking: isWorking,
        viewType: "month" as const,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.workConfigured) {
        this.refreshCurrentView();
      }
    });
  }

  /**
   * Open holiday detail dialog from day view
   */
  openHolidayDetailDayView(event: Event): void {
    event.stopPropagation();

    const dayData = this.calendarDayData();
    if (!dayData?.holiday) {
      return;
    }

    const isWorking =
      dayData.calendarDayStatus?.key === CalendarMonthDayStatusEnum.FREE;

    const dentistHolidayId = dayData.dentistHolidayId || 0;

    const dialogRef = this.dialog.open(HolidayDetailDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      data: {
        holiday: dayData.holiday,
        date: this.selectedDate,
        dentistHolidayId: dentistHolidayId,
        isWorking: isWorking,
        viewType: "day" as const,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.workConfigured) {
        this.refreshCurrentView();
      }
    });
  }

  /**
   * Open holiday detail dialog from week view
   */
  openHolidayDetailWeekView(date: Date, event: Event): void {
    event.stopPropagation();

    const dayData = this.getWeekDayData(date);
    if (!dayData?.holiday) {
      return;
    }

    const isWorking =
      dayData.calendarDayStatus?.key === CalendarMonthDayStatusEnum.FREE;

    const dentistHolidayId = dayData.dentistHolidayId || 0;

    const dialogRef = this.dialog.open(HolidayDetailDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      data: {
        holiday: dayData.holiday,
        date: date,
        dentistHolidayId: dentistHolidayId,
        isWorking: isWorking,
        viewType: "week" as const,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.workConfigured) {
        this.refreshCurrentView();
      }
    });
  }

  /**
   * Check if the current day view is FULL
   */
  isDayViewFull(): boolean {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.key === CalendarMonthDayStatusEnum.FULL;
  }

  /**
   * Check if the current day view has LOCKED slots
   */
  isDayViewLocked(): boolean {
    const dayData = this.calendarDayData();
    if (!dayData?.slots) {
      return false;
    }
    return dayData.slots.some((slot) => slot.status === SlotStatusEnum.LOCKED);
  }

  /**
   * Check if the current day view is FREE (available)
   */
  isDayViewFree(): boolean {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.key === CalendarMonthDayStatusEnum.FREE;
  }

  /**
   * Get description for FREE day in day view
   */
  getDayViewFreeDescription(): string {
    const dayData = this.calendarDayData();
    if (dayData?.calendarDayStatus?.description) {
      return dayData.calendarDayStatus.description;
    }
    return "Disponible";
  }

  /**
   * Get color for FREE badge in day view
   */
  getDayViewFreeColor(): string {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.color || "#10b981";
  }

  /**
   * Check if the current day view is a HOLIDAY
   */
  isDayViewHoliday(): boolean {
    const dayData = this.calendarDayData();
    return !!dayData?.holiday && !!dayData.holiday.description;
  }

  /**
   * Get description for HOLIDAY day in day view
   */
  getDayViewHolidayDescription(): string {
    const dayData = this.calendarDayData();
    if (dayData?.holiday?.description) {
      return dayData.holiday.description;
    }

    return "Feriado";
  }

  /**
   * Get slots for the current day view (RESERVED, LOCKED, and NOT_AVAILABLE slots)
   */
  getDayViewSlots() {
    const dayData = this.calendarDayData();

    if (!dayData?.slots) {
      return [];
    }

    return dayData.slots.filter((slot) => {
      return (
        (slot.status === "RESERVED" ||
          slot.status === "LOCKED" ||
          slot.status === "NOT_AVAILABLE") &&
        slot.startTime &&
        slot.endTime &&
        typeof slot.startTime === "string" &&
        typeof slot.endTime === "string"
      );
    });
  }

  /**
   * Get slots for a specific day in week view (RESERVED, LOCKED, and NOT_AVAILABLE slots)
   */
  getWeekViewSlots(date: Date) {
    const weekData = this.calendarWeekData();

    if (!weekData?.days) {
      return [];
    }

    const dayData = weekData.days.find((day) => {
      const dayDate = new Date(day.day);
      return (
        dayDate.getUTCFullYear() === date.getFullYear() &&
        dayDate.getUTCMonth() === date.getMonth() &&
        dayDate.getUTCDate() === date.getDate()
      );
    });

    if (!dayData?.slots) {
      return [];
    }

    return dayData.slots.filter((slot) => {
      return (
        (slot.status === "RESERVED" ||
          slot.status === "LOCKED" ||
          slot.status === "NOT_AVAILABLE") &&
        slot.startTime &&
        slot.endTime &&
        typeof slot.startTime === "string" &&
        typeof slot.endTime === "string"
      );
    });
  }

  /**
   * Get day data from week view for a specific date
   */
  getWeekDayData(date: Date): CalendarDayInterface | null {
    const weekData = this.calendarWeekData();

    if (!weekData?.days) {
      return null;
    }

    const dayData = weekData.days.find((day) => {
      const dayDate = new Date(day.day);
      return (
        dayDate.getUTCFullYear() === date.getFullYear() &&
        dayDate.getUTCMonth() === date.getMonth() &&
        dayDate.getUTCDate() === date.getDate()
      );
    });

    return dayData || null;
  }

  /**
   * Check if a day in week view is a HOLIDAY
   */
  isWeekDayHoliday(date: Date): boolean {
    const dayData = this.getWeekDayData(date);
    return !!dayData?.holiday && !!dayData.holiday.description;
  }

  /**
   * Get description for a day in week view (for HOLIDAY days)
   */
  getWeekDayHolidayDescription(date: Date): string | null {
    const dayData = this.getWeekDayData(date);
    // Get description from the holiday attribute
    if (dayData?.holiday?.description) {
      return dayData.holiday.description;
    }

    return "Feriado";
  }

  /**
   * Check if a day in week view is LOCKED
   */
  isWeekDayLocked(date: Date): boolean {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.key === ("LOCKED" as any);
  }

  /**
   * Get description for a day in week view (for LOCKED days)
   */
  getWeekDayLockedDescription(date: Date): string | null {
    const dayData = this.getWeekDayData(date);
    if (dayData?.calendarDayStatus?.description) {
      return dayData.calendarDayStatus.description;
    }

    return "Día bloqueado";
  }

  /**
   * Get color for holiday badge in day view
   */
  getDayViewHolidayColor(): string {
    const dayData = this.calendarDayData();
    return dayData?.holiday?.color || "#48925f";
  }

  /**
   * Get color for full badge in day view
   */
  getDayViewFullColor(): string {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.color || "#ef4444";
  }

  /**
   * Get color for locked badge in day view
   */
  getDayViewLockedColor(): string {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.color || "#f59e0b";
  }

  /**
   * Get color for holiday badge in week view
   */
  getWeekDayHolidayColor(date: Date): string {
    const dayData = this.getWeekDayData(date);
    return dayData?.holiday?.color || "#48925f";
  }

  /**
   * Check if a day in week view is FULL
   */
  isWeekDayFull(date: Date): boolean {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.key === ("FULL" as any);
  }

  /**
   * Check if a day in week view is FREE (available)
   */
  isWeekDayFree(date: Date): boolean {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.key === ("FREE" as any);
  }

  /**
   * Get description for a FREE day in week view
   */
  getWeekDayFreeDescription(date: Date): string | null {
    const dayData = this.getWeekDayData(date);
    if (dayData?.calendarDayStatus?.description) {
      return dayData.calendarDayStatus.description;
    }
    return "Disponible";
  }

  /**
   * Get color for FREE badge in week view
   */
  getWeekDayFreeColor(date: Date): string {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.color || "#10b981";
  }

  /**
   * Get color for full badge in week view
   */
  getWeekDayFullColor(date: Date): string {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.color || "#ef4444";
  }

  /**
   * Get color for locked badge in week view
   */
  getWeekDayLockedColor(date: Date): string {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.color || "#f59e0b";
  }

  /**
   * Get the list of lockType values from dentistLock array for a day in month view
   */
  getDentistLockTypes(date: Date): string[] {
    const dayData = this.getDayFromBackend(date);
    if (!dayData?.dentistLock || dayData.dentistLock.length === 0) {
      return [];
    }
    return dayData.dentistLock.map((lock) => lock.lockType);
  }

  /**
   * Open unlock dialog from month view badge.
   * Uses DentistLockMonthInterface which only has dentistCalendarLockId.
   * Constructs a minimal CalendarLockDayInterface with just the id needed by the serializer.
   */
  openUnlockFromMonthBadge(
    event: Event,
    lock: DentistLockMonthInterface,
  ): void {
    event.stopPropagation();

    const minimalLock: CalendarLockDayInterface = {
      id: lock.dentistCalendarLockId,
      idDentist: this.personId,
      appointmentConflict: null,
      startDate: "",
      startTime: "",
      endDate: "",
      endTime: "",
      lockType: lock.lockType,
      observation: "",
      observationUpdate: null,
      recurrence: "",
    };

    const dialogRef = this.dialog.open(UnlockCalendarDialog, {
      data: { calendarLock: minimalLock },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed && result?.observation) {
        this.calendarLockService
          .update(minimalLock, result.observation, this.personId)
          .subscribe({
            next: (response) => {
              this.snackbarService.openSnackbar(
                response.message || "Bloqueo eliminado exitosamente",
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success,
              );
              this.refreshCurrentView();
            },
          });
      }
    });
  }

  /**
   * Open unlock dialog from week view badge.
   * Uses CalendarLockDayInterface which already has all fields.
   */
  openUnlockFromWeekBadge(event: Event, lock: CalendarLockDayInterface): void {
    event.stopPropagation();

    const dialogRef = this.dialog.open(UnlockCalendarDialog, {
      data: { calendarLock: lock },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed && result?.observation) {
        this.calendarLockService
          .update(lock, result.observation, this.personId)
          .subscribe({
            next: (response) => {
              this.snackbarService.openSnackbar(
                response.message || "Bloqueo eliminado exitosamente",
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success,
              );
              this.refreshCurrentView();
            },
          });
      }
    });
  }

  /**
   * Get the list of lockType values from dentistLock array for a day in week view
   */
  getWeekDentistLockTypes(date: Date): string[] {
    const dayData = this.getWeekDayData(date);
    if (!dayData?.dentistLock || dayData.dentistLock.length === 0) {
      return [];
    }
    return dayData.dentistLock.map((lock) => lock.lockType);
  }

  /**
   * Calculate the top position of a slot in pixels based on its start time
   * Each hour = 60px (matching the time-slot height)
   */
  getSlotPosition(startTime: string): number {
    if (!startTime) return 0;
    const [hours, minutes] = this.parseTime(startTime);
    const totalMinutes = hours * 60 + minutes;
    return totalMinutes + 4;
  }

  /**
   * Calculate the top position of a slot in pixels for WEEK view
   * Adds offset to avoid overlapping with hour lines
   */
  getSlotPositionWeek(startTime: string): number {
    if (!startTime) return 4;
    const [hours, minutes] = this.parseTime(startTime);
    const totalMinutes = hours * 60 + minutes;
    return totalMinutes + 8;
  }

  /**
   * Calculate the height of a slot in pixels based on start and end time
   */
  getSlotHeight(startTime: string, endTime: string): number {
    if (!startTime || !endTime) return 56;
    const [startHours, startMinutes] = this.parseTime(startTime);
    const [endHours, endMinutes] = this.parseTime(endTime);

    const startTotalMinutes = startHours * 60 + startMinutes;
    const endTotalMinutes = endHours * 60 + endMinutes;
    const durationMinutes = endTotalMinutes - startTotalMinutes;

    const height = durationMinutes - 4;
    return Math.max(height, 15);
  }

  /**
   * Parse time string (HH:mm:ss or HH:mm) to hours and minutes
   */
  private parseTime(timeString: string): [number, number] {
    if (!timeString || typeof timeString !== "string") {
      return [0, 0];
    }

    const parts = timeString.split(":");
    if (parts.length < 2) {
      return [0, 0];
    }

    const hours = parseInt(parts[0], 10) || 0;
    const minutes = parseInt(parts[1], 10) || 0;
    return [hours, minutes];
  }

  setWorkHours(startHour: number, endHour: number) {
    this.workStartHour = startHour;
    this.workEndHour = endHour;
    this.timeSlots = [];
    this.generateTimeSlots();
  }

  isNightTime(timeSlot: string): boolean {
    const [hours] = timeSlot.split(":").map(Number);
    return hours >= 22 || hours < 6;
  }

  getCurrentTimePosition(): number {
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();

    const totalMinutes = currentHour * 60 + currentMinute;
    const slotMinutes = 60;
    const slotHeight = 40;

    const position = (totalMinutes / slotMinutes) * slotHeight;
    return position;
  }

  isCurrentDay(): boolean {
    const today = new Date();
    return this.selectedDate.toDateString() === today.toDateString();
  }

  startTimeUpdate() {
    this.updateCurrentTime();
    this.updateCurrentDayKey();

    setInterval(() => {
      this.updateCurrentTime();
      this.checkDayChange();
    }, 60000);
  }

  updateCurrentTime() {
    if (this.isCurrentDay()) {
    }
  }

  /**
   * Update the current day key
   */
  private updateCurrentDayKey() {
    const now = new Date();
    this.currentDayKey = `${now.getFullYear()}-${String(
      now.getMonth() + 1,
    ).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
  }

  /**
   * Check if the day has changed and reload day view if necessary
   */
  private checkDayChange() {
    const now = new Date();
    const newDayKey = `${now.getFullYear()}-${String(
      now.getMonth() + 1,
    ).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;

    if (newDayKey !== this.currentDayKey) {
      this.currentDayKey = newDayKey;

      if (this.currentView === "day") {
        this.loadDayView();
      }
    }
  }

  setupScrollSync() {
    setTimeout(() => {
      if (this.timeColumn && this.eventsColumn) {
        const timeColumnElement = this.timeColumn.nativeElement;
        const eventsColumnElement = this.eventsColumn.nativeElement;

        if (this.scrollHandler) {
          eventsColumnElement.removeEventListener("scroll", this.scrollHandler);
        }

        const eventsScrollHandler = () => {
          timeColumnElement.scrollTop = eventsColumnElement.scrollTop;
        };
        eventsColumnElement.addEventListener("scroll", eventsScrollHandler, {
          passive: true,
        });

        this.scrollHandler = eventsScrollHandler;
      }
    }, 300);
  }
  toggleMyCalendars() {
    this.myCalendarsExpanded = !this.myCalendarsExpanded;
  }

  toggleOtherCalendars() {
    this.otherCalendarsExpanded = !this.otherCalendarsExpanded;
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  createAppointment() {
    const userRole = this.localStorageService.getUserRole();

    const dialogRef = this.dialog.open(CreateAppointmentDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      data: {
        idDentist: userRole === RoleEnum.DENTIST ? this.personId : undefined,
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
        this.refreshCurrentView();
      }
    });
  }

  /**
   * Refresca la vista actual del calendario
   */
  private refreshCurrentView(): void {
    this.monthCache.clear();

    switch (this.currentView) {
      case "month":
        this.loadMonthView();
        break;
      case "week":
        this.loadWeekView();
        break;
      case "day":
        this.loadDayView();
        break;
    }
  }

  goToAvailability() {
    this.router.navigate(["/dentist-availability/" + this.personId]);
  }

  loadConflicts() {
    this.appointmentService.getAppointmentConflicts(this.personId).subscribe({
      next: (response) => {
        if (response.data && response.data.length > 0) {
          this.appointmentConflicts.set(response.data);
          this.hasConflicts.set(true);
        } else {
          this.appointmentConflicts.set([]);
          this.hasConflicts.set(false);
        }
      },
      error: (error) => {
        console.error("Error loading conflicts:", error);
        this.appointmentConflicts.set([]);
        this.hasConflicts.set(false);
      },
    });
  }

  viewConflicts() {
    const dialogRef = this.dialog.open(AppointmentsConflictDialogComponent, {
      data: {
        conflicts: this.appointmentConflicts(),
        allowReschedule: true,
      },
      width: "800px",
      maxWidth: "90vw",
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadConflicts();
      this.refreshCurrentView();
    });
  }

  getConflictCount(): string {
    const count = this.appointmentConflicts().length;
    return count > 0 ? count.toString() : "";
  }

  private getCurrentTimeSlot(): string {
    const now = new Date();
    const hour = now.getHours().toString().padStart(2, "0");
    const minute = now.getMinutes().toString().padStart(2, "0");
    return `${hour}:${minute}`;
  }

  createCalendarLock() {
    const dialogRef = this.dialog.open(CreateCalendarLockDialogComponent, {
      width: "800px",
      data: {
        personId: this.personId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.refreshCurrentView();
      }
    });
  }

  openSlotDetail(slot: any): void {
    if (slot.status === "LOCKED") {
      const dialogRef = this.dialog.open(CalendarLockDetailDialogComponent, {
        width: "600px",
        maxWidth: "90vw",
        data: {
          slot: slot,
          idDentist: this.personId,
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result?.unlocked) {
          this.refreshCurrentView();
        }
      });
    } else {
      const dialogRef = this.dialog.open(AppointmentDetailDialogComponent, {
        width: "600px",
        maxWidth: "90vw",
        data: {
          slot: slot,
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result?.cancelled || result?.rescheduled) {
          this.refreshCurrentView();
          this.loadConflicts();
        }
      });
    }
  }

  /**
   * Carga todos los dentistas y los agrupa por especialidad
   */
  loadDentists(): void {
    this.isLoadingDentists.set(true);

    this.dentistService.getAll().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.groupDentistsBySpecialty(response.data);
        }
        this.isLoadingDentists.set(false);
      },
      error: (error) => {
        console.error("Error al cargar dentistas:", error);
        this.snackbarService.openSnackbar(
          "Error al cargar la lista de dentistas",
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Error,
        );
        this.isLoadingDentists.set(false);
      },
    });
  }

  /**
   * Agrupa los dentistas por especialidad
   */
  private groupDentistsBySpecialty(dentists: DentistDto[]): void {
    const groupMap = new Map<string, DentistDto[]>();

    dentists.forEach((dentist) => {
      let specialty =
        typeof dentist.dentistSpecialty === "string"
          ? dentist.dentistSpecialty
          : (dentist.dentistSpecialty as any)?.name || "Sin especialidad";

      specialty = specialty.replace(/^[\"']|[\"']$/g, "").trim();

      if (!groupMap.has(specialty)) {
        groupMap.set(specialty, []);
      }
      groupMap.get(specialty)!.push(dentist);
    });

    const groups: SpecialtyGroup[] = Array.from(groupMap.entries()).map(
      ([specialtyName, dentists]) => ({
        specialtyName,
        dentists,
      }),
    );

    this.specialtyGroups.set(groups);
  }

  /**
   * Maneja la selección de un dentista
   */
  onDentistSelect(dentist: DentistDto): void {
    this.selectedDentist.set(dentist);
    this.personId = dentist.person.id;
    this.showDentistSelection.set(false);

    this.selectedSidebarDentistId.set(dentist.person.id);

    if (this.currentView === "month") {
      this.loadMonthView();
    } else if (this.currentView === "week") {
      this.loadWeekView();
    } else if (this.currentView === "day") {
      this.loadDayView();
    }
  }

  /**
   * Vuelve a la vista de selección de dentistas
   */
  backToDentistSelection(): void {
    this.showDentistSelection.set(true);
    this.selectedDentist.set(null);
    this.personId = 0;

    this.selectedSidebarDentistId.set(null);

    this.calendarMonthData.set(null);
    this.calendarWeekData.set(null);
    this.calendarDayData.set(null);

    this.monthCache.clear();
  }

  /**
   * Obtiene el nombre completo de un dentista
   */
  getDentistFullName(dentist: DentistDto): string {
    return `${dentist.person.firstName} ${dentist.person.lastName}`;
  }

  /**
   * Carga la lista de dentistas para mostrar en el sidebar
   */
  loadSidebarDentists(): void {
    this.dentistService.getAll().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.sidebarDentists.set(response.data);
        }
      },
      error: (error) => {
        console.error("Error al cargar dentistas del sidebar:", error);
      },
    });
  }

  /**
   * Maneja la selección de un dentista desde el sidebar
   */
  onSidebarDentistSelect(dentist: DentistDto): void {
    const currentSelected = this.selectedSidebarDentistId();

    if (currentSelected === dentist.person.id) {
      return;
    }

    this.selectedSidebarDentistId.set(dentist.person.id);
    this.personId = dentist.person.id;

    if (this.localStorageService.isSecretary()) {
      this.selectedDentist.set(dentist);
      this.showDentistSelection.set(false);
    }
    this.monthCache.clear();
    this.calendarMonthData.set(null);
    this.calendarWeekData.set(null);
    this.calendarDayData.set(null);
    if (this.currentView === "month") {
      this.loadMonthView();
    } else if (this.currentView === "week") {
      this.loadWeekView();
    } else if (this.currentView === "day") {
      this.loadDayView();
    }
  }

  /**
   * Maneja la selección del calendario del usuario logueado ("Yo")
   */
  onMyCalendarSelect(): void {
    const userId = this.localStorageService.getUserData()?.person.id;

    if (!userId) {
      console.error("No se pudo obtener el ID del usuario logueado");
      return;
    }

    if (this.selectedSidebarDentistId() === userId) {
      return;
    }
    this.selectedSidebarDentistId.set(userId);
    this.personId = userId;

    this.monthCache.clear();
    this.calendarMonthData.set(null);
    this.calendarWeekData.set(null);
    this.calendarDayData.set(null);
    if (this.currentView === "month") {
      this.loadMonthView();
    } else if (this.currentView === "week") {
      this.loadWeekView();
    } else if (this.currentView === "day") {
      this.loadDayView();
    }
  }

  /**
   * Verifica si un dentista está seleccionado en el sidebar
   */
  isSidebarDentistSelected(dentistId: number): boolean {
    return this.selectedSidebarDentistId() === dentistId;
  }

  /**
   * Verifica si el día actual tiene turnos que se pueden cancelar
   * (tiene slots RESERVED)
   */
  canCancelDayViewAppointments(): boolean {
    const dayData = this.calendarDayData();

    if (!dayData) {
      return false;
    }

    const hasReservedSlots = dayData.slots?.some(
      (slot) => slot.status === "RESERVED",
    );

    return hasReservedSlots || false;
  }

  /**
   * Verifica si un día específico en la vista semanal tiene turnos que se pueden cancelar
   * (tiene slots RESERVED)
   */
  canCancelWeekDayAppointments(date: Date): boolean {
    const dayData = this.getWeekDayData(date);

    if (!dayData) {
      return false;
    }

    const hasReservedSlots = dayData.slots?.some(
      (slot) => slot.status === "RESERVED",
    );

    return hasReservedSlots || false;
  }

  /**
   * Cancela todos los turnos de un día específico
   */
  cancelAllAppointments(date: Date): void {
    const dialogRef = this.dialog.open(CancelAllAppointmentsDialog, {
      width: "600px",
      maxWidth: "90vw",
      data: {
        date: date,
        dentistId: this.personId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed && result?.observation) {
        const appointment: AppointmentInterface = {
          patient: {} as PatientInterface,
          dentist: {} as PersonInterface,
          dateTime: date,
          observation: result.observation,
          requestSource: RequestSourceEnum.DENTIST,
        };

        this.appointmentService
          .cancelAll(this.personId, date, appointment)
          .subscribe({
            next: (response) => {
              if (response.success) {
                this.snackbarService.openSnackbar(
                  "Todos los turnos del día fueron cancelados exitosamente",
                  6000,
                  "center",
                  "top",
                  SnackbarTypeEnum.Success,
                );
                this.refreshCurrentView();
                this.loadConflicts();
              }
            },
            error: (error) => {
              console.error("Error al cancelar todos los turnos:", error);
            },
          });
      }
    });
  }
}
