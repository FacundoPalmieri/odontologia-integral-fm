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
import { LoaderService } from "../../../services/loader.service";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatRadioModule } from "@angular/material/radio";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { CreateAppointmentDialogComponent } from "./create-appointment-dialog/create-appointment-dialog.component";
import { CreateCalendarLockDialogComponent } from "./create-calendar-lock-dialog/create-calendar-lock-dialog.component";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { CalendarService } from "../../../services/calendar.service";
import {
  CalendarMonthInterface,
  CalendarMonthDayInterface,
  CalendarWeekInterface,
  CalendarDayInterface,
} from "../../../domain/interfaces/calendar.interface";
import { CalendarMonthDayStatusEnum } from "../../../utils/enums/appointment/appointment-status.enum";
import { RoleEnum } from "../../../utils/enums/role.enum";
import { forkJoin } from "rxjs";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { DentistService } from "../../../services/dentist.service";
import { DentistDtoInterface } from "../../../domain/dto/dentist.dto";
import { ConflictDialogComponent } from "../conflict-dialog/conflict-dialog.component";
import { AppointmentService } from "../../../services/appointment.service";
import { CancelAllAppointmentsDialog } from "../cancel-all-appointments-dialog/cancel-all-appointments-dialog.component";
import { AppointmentInterface } from "../../../domain/interfaces/appointment.inteface";
import { RequestSourceEnum } from "../../../utils/enums/appointment/request-source.enum";

export type CalendarView = "day" | "week" | "month";

// Interfaz para agrupar dentistas por especialidad
export interface SpecialtyGroup {
  specialtyName: string;
  dentists: DentistDtoInterface[];
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
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly calendarService = inject(CalendarService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly dentistService = inject(DentistService);
  private readonly appointmentService = inject(AppointmentService);

  dialog = inject(MatDialog);
  loading$ = this.loaderService.loading$;

  personId: number = 0;

  @ViewChild("timeColumn", { static: false }) timeColumn!: ElementRef;
  @ViewChild("eventsColumn", { static: false }) eventsColumn!: ElementRef;

  private scrollHandler: (() => void) | null = null;

  // Calendar data signals
  isLoadingCalendar = signal<boolean>(false);
  calendarMonthData = signal<CalendarMonthInterface | null>(null);
  calendarWeekData = signal<CalendarWeekInterface | null>(null);
  calendarDayData = signal<CalendarDayInterface | null>(null);

  // Conflicts data
  appointmentConflicts = signal<any[]>([]);
  hasConflicts = signal<boolean>(false);

  // Cache para almacenar los meses ya cargados (key: "YYYY-MM", value: CalendarMonthInterface)
  private monthCache = new Map<string, CalendarMonthInterface>();

  // Track current day to detect day changes
  private currentDayKey: string = "";

  currentDate = new Date();
  selectedDate = new Date();
  currentView: CalendarView = "month";

  // Estados de expansión de secciones
  myCalendarsExpanded = true;
  otherCalendarsExpanded = true;
  sidebarCollapsed = false;

  // Propiedades para lista de dentistas en sidebar (todos los roles)
  sidebarDentists = signal<DentistDtoInterface[]>([]);
  selectedSidebarDentistId = signal<number | null>(null);

  // Propiedades para vista de secretario
  specialtyGroups = signal<SpecialtyGroup[]>([]);
  selectedDentist = signal<DentistDtoInterface | null>(null);
  isLoadingDentists = signal<boolean>(false);
  showDentistSelection = signal<boolean>(false);

  // Configuración de horarios de trabajo
  workStartHour = 0; // 12:00 AM (medianoche)
  workEndHour = 24; // 12:00 AM (medianoche del día siguiente)

  // Horarios para vista de día (cada 30 minutos)
  timeSlots: string[] = [];

  // Días de la semana
  weekDays = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];

  // Meses
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
    this.personId = this.authService.getUserData()?.person.id || 0;
    this.updateSelectedDate();

    // Si es secretario, cargar dentistas y mostrar vista de selección
    if (this.authService.isSecretary()) {
      this.sidebarCollapsed = true; // Colapsar sidebar por defecto para secretarios
      this.showDentistSelection.set(true);
      this.loadDentists();
      // Cargar lista de dentistas para el sidebar
      this.loadSidebarDentists();
    } else if (this.authService.isAdministrator()) {
      // Para administradores, cargar lista de dentistas para el sidebar
      this.loadSidebarDentists();
      // Establecer el dentista actual como seleccionado
      this.selectedSidebarDentistId.set(this.personId);

      // Cargar conflictos
      this.loadConflicts();

      if (this.currentView === "month") {
        this.loadMonthView();
      } else if (this.currentView === "week") {
        this.loadWeekView();
      } else if (this.currentView === "day") {
        this.loadDayView();
      }
    } else {
      // Para dentistas, NO cargar lista de dentistas (solo ven su propia agenda)
      // Establecer el dentista actual como seleccionado
      this.selectedSidebarDentistId.set(this.personId);

      // Cargar conflictos
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
    const month = this.selectedDate.getMonth() + 1; // JavaScript months are 0-indexed

    // Calcular mes anterior y siguiente
    const prevDate = new Date(year, month - 2, 1);
    const nextDate = new Date(year, month, 1);

    const prevYear = prevDate.getFullYear();
    const prevMonth = prevDate.getMonth() + 1;
    const nextYear = nextDate.getFullYear();
    const nextMonth = nextDate.getMonth() + 1;

    // Crear claves para el caché
    const prevKey = `${prevYear}-${String(prevMonth).padStart(2, "0")}`;
    const currentKey = `${year}-${String(month).padStart(2, "0")}`;
    const nextKey = `${nextYear}-${String(nextMonth).padStart(2, "0")}`;

    // Preparar las peticiones solo para los meses que no están en caché
    const requests: { [key: string]: any } = {};

    if (!this.monthCache.has(prevKey)) {
      requests["prev"] = this.calendarService.getMonth(
        this.personId,
        prevYear,
        prevMonth
      );
    }
    if (!this.monthCache.has(currentKey)) {
      requests["current"] = this.calendarService.getMonth(
        this.personId,
        year,
        month
      );
    }
    if (!this.monthCache.has(nextKey)) {
      requests["next"] = this.calendarService.getMonth(
        this.personId,
        nextYear,
        nextMonth
      );
    }

    // Si no hay peticiones pendientes, usar solo el caché
    if (Object.keys(requests).length === 0) {
      this.combineMonthsFromCache(prevKey, currentKey, nextKey);
      this.isLoadingCalendar.set(false);
      return;
    }

    // Hacer las peticiones pendientes en paralelo
    forkJoin(requests).subscribe({
      next: (responses: any) => {
        // Guardar en caché las respuestas nuevas
        if (responses["prev"]?.success && responses["prev"].data) {
          this.monthCache.set(prevKey, responses["prev"].data);
        }
        if (responses["current"]?.success && responses["current"].data) {
          this.monthCache.set(currentKey, responses["current"].data);
        }
        if (responses["next"]?.success && responses["next"].data) {
          this.monthCache.set(nextKey, responses["next"].data);
        }

        // Combinar todos los meses (desde caché y nuevos)
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
    nextKey: string
  ) {
    const allDays: CalendarMonthDayInterface[] = [];

    // Obtener días del caché
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

    // Crear el objeto con todos los días
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
    // Generar horarios de trabajo configurables (cada hora)
    for (let hour = this.workStartHour; hour < this.workEndHour; hour++) {
      const timeString = `${hour.toString().padStart(2, "0")}:00`;
      this.timeSlots.push(timeString);
    }
  }

  setView(view: CalendarView) {
    this.currentView = view;

    // Load data when switching views
    if (view === "month") {
      // Clear cache when switching to month view to ensure fresh data
      this.monthCache.clear();
      this.loadMonthView();
    } else if (view === "week") {
      this.loadWeekView();
    } else if (view === "day") {
      this.loadDayView();
    }

    // Reconfigurar sincronización cuando cambie la vista
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

    // Reload data when navigating
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

    // Reload data when going to today
    if (this.currentView === "month") {
      this.loadMonthView();
    } else if (this.currentView === "week") {
      this.loadWeekView();
    } else if (this.currentView === "day") {
      this.loadDayView();
    }
  }

  updateSelectedDate() {
    // Actualizar la fecha actual si es necesario
    this.currentDate = new Date();
  }

  getWeekDates(): Date[] {
    const startOfWeek = new Date(this.selectedDate);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day; // Ajustar para que la semana empiece en domingo
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
    startDate.setDate(startDate.getDate() - firstDay.getDay()); // Ajustar para que empiece en domingo

    const dates: Date[] = [];
    const current = new Date(startDate);

    // Calcular cuántas semanas necesitamos
    // Seguir agregando días hasta que hayamos pasado el último día del mes
    // y completado la semana actual
    while (
      current <= lastDay ||
      (current > lastDay && current.getDay() !== 0) // Completar la última semana hasta el domingo
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
   */
  isNotAvailableDay(date: Date): boolean {
    const dayData = this.getDayFromBackend(date);
    return dayData?.status === CalendarMonthDayStatusEnum.NOT_AVAILABLE;
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

    // Crear una clave única para la fecha (YYYY-MM-DD)
    const dateKey = `${date.getFullYear()}-${String(
      date.getMonth() + 1
    ).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

    // Buscar el día que coincida con la fecha
    return calendarData.days.find((day) => day.date === dateKey) || null;
  }

  /**
   * Get the color for a specific day from backend data
   */
  getDayColor(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    return dayData?.color || null;
  }

  /**
   * Get the description for a specific day (for holidays, full days, locked days, and free days)
   * NOT_AVAILABLE days are excluded from showing badges in monthly view
   */
  getDayDescription(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    // Mostrar descripción si es feriado, día completo, día bloqueado o libre (FREE)
    // NOT_AVAILABLE no muestra badge, solo se deshabilita visualmente
    if (
      dayData?.status === CalendarMonthDayStatusEnum.HOLIDAY ||
      dayData?.status === CalendarMonthDayStatusEnum.FULL ||
      dayData?.status === CalendarMonthDayStatusEnum.LOCKED ||
      dayData?.status === CalendarMonthDayStatusEnum.FREE
    ) {
      return dayData.description;
    }
    return null;
  }

  /**
   * Get the icon for a specific day based on its status
   */
  getDayIcon(date: Date): string {
    const dayData = this.getDayFromBackend(date);

    if (dayData?.status === CalendarMonthDayStatusEnum.HOLIDAY) {
      return "🎉"; // Celebración para feriados
    }

    if (dayData?.status === CalendarMonthDayStatusEnum.FULL) {
      return "⛔"; // Prohibido para días completos/sin turnos
    }

    if (dayData?.status === CalendarMonthDayStatusEnum.LOCKED) {
      return "🔒"; // Candado para días bloqueados
    }

    if (dayData?.status === CalendarMonthDayStatusEnum.FREE) {
      return "✅"; // Check para días libres/disponibles
    }

    return "📅"; // Calendario por defecto
  }

  /**
   * Check if the current day view is FULL
   */
  isDayViewFull(): boolean {
    const dayData = this.calendarDayData();
    // Check if calendarDayStatus.key is 'FULL'
    return dayData?.calendarDayStatus?.key === ("FULL" as any);
  }

  /**
   * Check if the current day view has LOCKED slots
   */
  isDayViewLocked(): boolean {
    const dayData = this.calendarDayData();
    if (!dayData?.slots) {
      return false;
    }
    return dayData.slots.some((slot) => slot.status === "LOCKED");
  }

  /**
   * Check if the current day view is FREE (available)
   */
  isDayViewFree(): boolean {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.key === ("FREE" as any);
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
    return dayData?.calendarDayStatus?.color || "#10b981"; // Verde por defecto
  }

  /**
   * Check if the current day view is a HOLIDAY
   */
  isDayViewHoliday(): boolean {
    const dayData = this.calendarDayData();
    // Check if the holiday attribute exists and has data
    return !!dayData?.holiday && !!dayData.holiday.description;
  }

  /**
   * Get description for HOLIDAY day in day view
   */
  getDayViewHolidayDescription(): string {
    const dayData = this.calendarDayData();
    // Get description from the holiday attribute
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

    // Filter to show RESERVED, LOCKED, and NOT_AVAILABLE slots
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

    // Find the day that matches the date
    // Use UTC methods for backend date (comes as ISO string) and local methods for calendar date
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

    // Filter to show RESERVED, LOCKED, and NOT_AVAILABLE slots
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

    // Find the day that matches the date
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
    // Check if the holiday attribute exists and has data
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
    // Check if calendarDayStatus.key is LOCKED (full day)
    return dayData?.calendarDayStatus?.key === ("LOCKED" as any);
  }

  /**
   * Get description for a day in week view (for LOCKED days)
   */
  getWeekDayLockedDescription(date: Date): string | null {
    const dayData = this.getWeekDayData(date);
    // Get description from calendarDayStatus
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
    return dayData?.holiday?.color || "#48925f"; // Púrpura por defecto
  }

  /**
   * Get color for full badge in day view
   */
  getDayViewFullColor(): string {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.color || "#ef4444"; // Rojo por defecto
  }

  /**
   * Get color for locked badge in day view
   */
  getDayViewLockedColor(): string {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus?.color || "#f59e0b"; // Naranja por defecto
  }

  /**
   * Get color for holiday badge in week view
   */
  getWeekDayHolidayColor(date: Date): string {
    const dayData = this.getWeekDayData(date);
    return dayData?.holiday?.color || "#48925f"; // Púrpura por defecto
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
    return dayData?.calendarDayStatus?.color || "#10b981"; // Verde por defecto
  }

  /**
   * Get color for full badge in week view
   */
  getWeekDayFullColor(date: Date): string {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.color || "#ef4444"; // Rojo por defecto
  }

  /**
   * Get color for locked badge in week view
   */
  getWeekDayLockedColor(date: Date): string {
    const dayData = this.getWeekDayData(date);
    return dayData?.calendarDayStatus?.color || "#f59e0b"; // Naranja por defecto
  }

  /**
   * Calculate the top position of a slot in pixels based on its start time
   * Each hour = 60px (matching the time-slot height)
   */
  getSlotPosition(startTime: string): number {
    if (!startTime) return 0;
    const [hours, minutes] = this.parseTime(startTime);
    const totalMinutes = hours * 60 + minutes;
    // 60px per hour = 1px per minute
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
    // 60px per hour = 1px per minute
    // Add 4px offset to avoid overlapping with hour line in week view
    return totalMinutes + 8;
  }

  /**
   * Calculate the height of a slot in pixels based on start and end time
   */
  getSlotHeight(startTime: string, endTime: string): number {
    if (!startTime || !endTime) return 56; // Default 1 hour minus gap
    const [startHours, startMinutes] = this.parseTime(startTime);
    const [endHours, endMinutes] = this.parseTime(endTime);

    const startTotalMinutes = startHours * 60 + startMinutes;
    const endTotalMinutes = endHours * 60 + endMinutes;
    const durationMinutes = endTotalMinutes - startTotalMinutes;

    // 1 minute = 1px (60px per hour)
    // Subtract 4px to create visual gap between consecutive slots
    const height = durationMinutes - 4;
    return Math.max(height, 15); // Minimum 15px height
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

  // Método para cambiar los horarios de trabajo
  setWorkHours(startHour: number, endHour: number) {
    this.workStartHour = startHour;
    this.workEndHour = endHour;
    this.timeSlots = [];
    this.generateTimeSlots();
  }

  // Método para determinar si un horario es nocturno
  isNightTime(timeSlot: string): boolean {
    const [hours] = timeSlot.split(":").map(Number);
    return hours >= 22 || hours < 6;
  }

  // Método para obtener la posición de la línea de tiempo actual
  getCurrentTimePosition(): number {
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();

    // Calcular la posición en píxeles (40px por slot de 1 hora)
    const totalMinutes = currentHour * 60 + currentMinute;
    const slotMinutes = 60; // Cada slot es de 1 hora
    const slotHeight = 40; // Altura de cada slot en píxeles

    const position = (totalMinutes / slotMinutes) * slotHeight;
    return position;
  }

  // Método para verificar si es el día actual
  isCurrentDay(): boolean {
    const today = new Date();
    return this.selectedDate.toDateString() === today.toDateString();
  }

  // Método para actualizar la línea de tiempo cada minuto
  startTimeUpdate() {
    // Actualizar inmediatamente
    this.updateCurrentTime();
    this.updateCurrentDayKey();

    // Actualizar cada minuto
    setInterval(() => {
      this.updateCurrentTime();
      this.checkDayChange();
    }, 60000); // 60 segundos
  }

  // Método para forzar la actualización de la línea de tiempo
  updateCurrentTime() {
    // Forzar la detección de cambios
    if (this.isCurrentDay()) {
      // La línea se actualizará automáticamente por el binding
    }
  }

  /**
   * Update the current day key
   */
  private updateCurrentDayKey() {
    const now = new Date();
    this.currentDayKey = `${now.getFullYear()}-${String(
      now.getMonth() + 1
    ).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
  }

  /**
   * Check if the day has changed and reload day view if necessary
   */
  private checkDayChange() {
    const now = new Date();
    const newDayKey = `${now.getFullYear()}-${String(
      now.getMonth() + 1
    ).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;

    if (newDayKey !== this.currentDayKey) {
      this.currentDayKey = newDayKey;

      // Reload day view if currently in day view
      if (this.currentView === "day") {
        this.loadDayView();
      }
    }
  }

  // Configurar sincronización de scroll
  setupScrollSync() {
    // Usar setTimeout para asegurar que los elementos estén disponibles
    setTimeout(() => {
      if (this.timeColumn && this.eventsColumn) {
        const timeColumnElement = this.timeColumn.nativeElement;
        const eventsColumnElement = this.eventsColumn.nativeElement;

        // Limpiar event listeners anteriores si existen
        if (this.scrollHandler) {
          eventsColumnElement.removeEventListener("scroll", this.scrollHandler);
        }

        // Solo la columna de eventos tiene scroll, la de horas se mueve programáticamente
        const eventsScrollHandler = () => {
          // Mover la columna de horas para que coincida con el scroll de eventos
          timeColumnElement.scrollTop = eventsColumnElement.scrollTop;
        };

        // Solo agregar listener a la columna de eventos
        eventsColumnElement.addEventListener("scroll", eventsScrollHandler, {
          passive: true,
        });

        // Guardar referencia para poder limpiar después
        this.scrollHandler = eventsScrollHandler;
      }
    }, 300); // Aumentar timeout para asegurar que los elementos estén listos
  }

  // Métodos para alternar expansión de secciones
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
    // Obtener el rol del usuario para determinar qué datos pasar
    const userRole = this.authService.getUserRole();

    const dialogRef = this.dialog.open(CreateAppointmentDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      data: {
        // Solo pasar idDentist si el usuario es DENTIST
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
          SnackbarTypeEnum.Success
        );
        this.refreshCurrentView();
      }
    });
  }

  /**
   * Refresca la vista actual del calendario
   */
  private refreshCurrentView(): void {
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
    this.router.navigate(["/dentist-availability/" + this.personId]); // Navegar a la ruta de disponibilidad
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
    // Usar los conflictos ya cargados y pasarlos al diálogo
    const dialogRef = this.dialog.open(ConflictDialogComponent, {
      data: {
        conflicts: this.appointmentConflicts(),
        allowReschedule: true, // Permitir reprogramar desde el calendario
      },
      width: "800px",
      maxWidth: "90vw",
    });

    // Al cerrar el diálogo, recargar conflictos y actualizar calendario
    dialogRef.afterClosed().subscribe(() => {
      this.loadConflicts(); // Recargar la lista de conflictos
      this.refreshCurrentView(); // Actualizar la vista del calendario
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

    // Suscribirse al cierre del diálogo para recargar la vista
    dialogRef.afterClosed().subscribe((result) => {
      console.log(result);
      if (result?.success) {
        // Si se creó un bloqueo exitosamente, recargar la vista actual
        this.refreshCurrentView();
      }
    });
  }

  openSlotDetail(slot: any): void {
    // Importar dinámicamente el componente para evitar problemas de dependencias
    import(
      "./appointment-detail-dialog/appointment-detail-dialog.component"
    ).then((module) => {
      const dialogRef = this.dialog.open(
        module.AppointmentDetailDialogComponent,
        {
          width: "600px",
          maxWidth: "90vw",
          data: {
            slot: slot,
          },
        }
      );

      // Al cerrar el diálogo, recargar la vista si se canceló la cita
      dialogRef.afterClosed().subscribe((result) => {
        if (result?.cancelled) {
          this.refreshCurrentView();
          this.loadConflicts(); // También recargar conflictos si los hay
        }
      });
    });
  }

  // ===== MÉTODOS PARA VISTA DE SECRETARIO =====

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
          SnackbarTypeEnum.Error
        );
        this.isLoadingDentists.set(false);
      },
    });
  }

  /**
   * Agrupa los dentistas por especialidad
   */
  private groupDentistsBySpecialty(dentists: DentistDtoInterface[]): void {
    const groupMap = new Map<string, DentistDtoInterface[]>();

    dentists.forEach((dentist) => {
      // Manejar tanto string como objeto para dentistSpecialty
      let specialty =
        typeof dentist.dentistSpecialty === "string"
          ? dentist.dentistSpecialty
          : (dentist.dentistSpecialty as any)?.name || "Sin especialidad";

      // Limpiar comillas del string si existen
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
      })
    );

    this.specialtyGroups.set(groups);
  }

  /**
   * Maneja la selección de un dentista
   */
  onDentistSelect(dentist: DentistDtoInterface): void {
    this.selectedDentist.set(dentist);
    this.personId = dentist.person.id;
    this.showDentistSelection.set(false);

    // Sincronizar el radio button del sidebar
    this.selectedSidebarDentistId.set(dentist.person.id);

    // Cargar el calendario del dentista seleccionado
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

    // Limpiar la selección del sidebar
    this.selectedSidebarDentistId.set(null);

    // Limpiar datos del calendario
    this.calendarMonthData.set(null);
    this.calendarWeekData.set(null);
    this.calendarDayData.set(null);

    // Limpiar el caché de meses para evitar mostrar datos del dentista anterior
    this.monthCache.clear();
  }

  /**
   * Obtiene el nombre completo de un dentista
   */
  getDentistFullName(dentist: DentistDtoInterface): string {
    return `${dentist.person.firstName} ${dentist.person.lastName}`;
  }

  // ===== MÉTODOS PARA SIDEBAR DE DENTISTAS (TODOS LOS ROLES) =====

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
  onSidebarDentistSelect(dentist: DentistDtoInterface): void {
    // Solo permitir un dentista seleccionado a la vez
    const currentSelected = this.selectedSidebarDentistId();

    if (currentSelected === dentist.person.id) {
      // Si ya está seleccionado, no hacer nada
      return;
    }

    // Actualizar selección
    this.selectedSidebarDentistId.set(dentist.person.id);
    this.personId = dentist.person.id;

    // Si es secretario, también actualizar selectedDentist y ocultar vista de selección
    if (this.authService.isSecretary()) {
      this.selectedDentist.set(dentist);
      this.showDentistSelection.set(false);
    }

    // Limpiar caché y datos anteriores
    this.monthCache.clear();
    this.calendarMonthData.set(null);
    this.calendarWeekData.set(null);
    this.calendarDayData.set(null);

    // Cargar calendario del dentista seleccionado
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
    const userId = this.authService.getUserData()?.person.id;

    if (!userId) {
      console.error("No se pudo obtener el ID del usuario logueado");
      return;
    }

    // Si ya está seleccionado, no hacer nada
    if (this.selectedSidebarDentistId() === userId) {
      return;
    }

    // Actualizar selección
    this.selectedSidebarDentistId.set(userId);
    this.personId = userId;

    // Limpiar caché y datos anteriores
    this.monthCache.clear();
    this.calendarMonthData.set(null);
    this.calendarWeekData.set(null);
    this.calendarDayData.set(null);

    // Cargar calendario del usuario logueado
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

    // Verificar si hay slots RESERVED
    const hasReservedSlots = dayData.slots?.some(
      (slot) => slot.status === "RESERVED"
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

    // Verificar si hay slots RESERVED
    const hasReservedSlots = dayData.slots?.some(
      (slot) => slot.status === "RESERVED"
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
        // Crear el objeto de cita con la observación y el requestSource
        const appointment: AppointmentInterface = {
          observation: result.observation,
          requestSource: RequestSourceEnum.DENTIST,
        } as AppointmentInterface;

        // Llamar al servicio para cancelar todos los turnos
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
                  SnackbarTypeEnum.Success
                );
                this.refreshCurrentView();
                this.loadConflicts(); // Recargar conflictos si los hay
              }
            },
            error: (error) => {
              console.error("Error al cancelar todos los turnos:", error);
              this.snackbarService.openSnackbar(
                "Error al cancelar los turnos del día",
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Error
              );
            },
          });
      }
    });
  }
}
