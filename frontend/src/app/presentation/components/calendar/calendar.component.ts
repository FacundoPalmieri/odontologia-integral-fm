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
import { LoaderService } from "../../../services/loader.service";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatCheckboxModule } from "@angular/material/checkbox";
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
import { forkJoin } from "rxjs";

export interface CalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  color?: string;
  description?: string;
}

export type CalendarView = "day" | "week" | "month";

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
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
  ],
})
export class CalendarComponent implements OnInit, AfterViewInit {
  private readonly loaderService = inject(LoaderService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly calendarService = inject(CalendarService);

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

  // Configuración de horarios de trabajo
  workStartHour = 0; // 12:00 AM (medianoche)
  workEndHour = 24; // 12:00 AM (medianoche del día siguiente)

  // Eventos de ejemplo distribuidos en octubre y noviembre de 2025
  events: CalendarEvent[] = [];

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

    // Load month view data on initialization
    if (this.currentView === "month") {
      this.loadMonthView();
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

  getEventsForDate(date: Date): CalendarEvent[] {
    return this.events.filter((event) => {
      const eventDate = new Date(event.start);
      return eventDate.toDateString() === date.toDateString();
    });
  }

  getEventsForTimeSlot(date: Date, timeSlot: string): CalendarEvent[] {
    const [hours] = timeSlot.split(":").map(Number);
    const slotStart = new Date(date);
    slotStart.setHours(hours, 0, 0, 0);
    const slotEnd = new Date(slotStart);
    slotEnd.setHours(slotEnd.getHours() + 1);

    return this.events.filter((event) => {
      const eventStart = new Date(event.start);
      const eventEnd = new Date(event.end);

      // Verificar que el evento sea del mismo día
      const isSameDay = eventStart.toDateString() === date.toDateString();

      // Verificar si el evento se superpone con el slot de tiempo
      const overlaps =
        (eventStart >= slotStart && eventStart < slotEnd) ||
        (eventEnd > slotStart && eventEnd <= slotEnd) ||
        (eventStart <= slotStart && eventEnd >= slotEnd);

      return isSameDay && overlaps;
    });
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  }

  isCurrentMonth(date: Date): boolean {
    return date.getMonth() === this.selectedDate.getMonth();
  }

  formatDate(date: Date): string {
    return date.toLocaleDateString("es-ES", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }

  formatWeekDate(date: Date): string {
    return date.toLocaleDateString("es-ES", {
      month: "short",
      day: "numeric",
    });
  }

  onDateClick(date: Date) {
    this.selectedDate = date;
    this.currentView = "day";
    // Load day view data when clicking on a date
    this.loadDayView();
  }

  selectDate(date: Date) {
    this.selectedDate = date;
  }

  onEventClick(event: CalendarEvent) {
    // Aquí puedes abrir un diálogo o navegar a los detalles del evento
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
   * Get the color with opacity for a specific day
   * Converts hex color to rgba with specified opacity
   */
  getDayColorWithOpacity(date: Date, opacity: number = 0.4): string | null {
    const color = this.getDayColor(date);
    if (!color) return null;

    // Convertir hex a rgb
    const hex = color.replace("#", "");
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);

    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
  }

  /**
   * Get the description for a specific day (for holidays, full days, locked days, and not available days)
   */
  getDayDescription(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    // Mostrar descripción si es feriado, día completo, día bloqueado o no disponible
    if (
      dayData?.status === CalendarMonthDayStatusEnum.HOLIDAY ||
      dayData?.status === CalendarMonthDayStatusEnum.FULL ||
      dayData?.status === CalendarMonthDayStatusEnum.LOCKED ||
      dayData?.status === CalendarMonthDayStatusEnum.NOT_AVAILABLE
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

    if (dayData?.status === CalendarMonthDayStatusEnum.NOT_AVAILABLE) {
      return "🚫"; // No disponible
    }

    return "📅"; // Calendario por defecto
  }

  /**
   * Check if the current day view is FULL
   */
  isDayViewFull(): boolean {
    const dayData = this.calendarDayData();
    // Backend sends 'FULL' as string even though it's not in SlotStatusEnum
    return dayData?.calendarDayStatus === ("FULL" as any);
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
   * Check if the current day view is completely NOT_AVAILABLE
   */
  isDayViewNotAvailable(): boolean {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus === ("NOT_AVAILABLE" as any);
  }

  /**
   * Get description for NOT_AVAILABLE day in day view
   */
  getDayViewNotAvailableDescription(): string {
    const dayData = this.calendarDayData();

    // Try to get description from the first NOT_AVAILABLE slot
    if (dayData?.slots) {
      const notAvailableSlot = dayData.slots.find(
        (slot) => slot.status === "NOT_AVAILABLE"
      );
      return notAvailableSlot?.calendarLock?.observation || "No disponible";
    }

    return "No disponible";
  }

  /**
   * Check if the current day view is a HOLIDAY
   */
  isDayViewHoliday(): boolean {
    const dayData = this.calendarDayData();
    return dayData?.calendarDayStatus === ("HOLIDAY" as any);
  }

  /**
   * Get description for HOLIDAY day in day view
   */
  getDayViewHolidayDescription(): string {
    // For HOLIDAY days, we need to get the description from the month data
    // since the day view doesn't contain the holiday description (slots are empty)
    const monthDay = this.getDayFromBackend(this.selectedDate);
    if (monthDay?.description) {
      return monthDay.description;
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
        slot.starTime &&
        slot.endTime &&
        typeof slot.starTime === "string" &&
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
        slot.starTime &&
        slot.endTime &&
        typeof slot.starTime === "string" &&
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
   * Check if a day in week view is completely NOT_AVAILABLE
   */
  isWeekDayNotAvailable(date: Date): boolean {
    const dayData = this.getWeekDayData(date);
    // Check if calendarDayStatus is NOT_AVAILABLE (full day)
    return dayData?.calendarDayStatus === ("NOT_AVAILABLE" as any);
  }

  /**
   * Get description for a day in week view (for NOT_AVAILABLE days)
   */
  getWeekDayDescription(date: Date): string | null {
    const dayData = this.getWeekDayData(date);

    // If the whole day is NOT_AVAILABLE, try to get description from the first NOT_AVAILABLE slot
    if (this.isWeekDayNotAvailable(date) && dayData?.slots) {
      const notAvailableSlot = dayData.slots.find(
        (slot) => slot.status === "NOT_AVAILABLE"
      );
      return notAvailableSlot?.calendarLock?.observation || "No trabaja";
    }

    return null;
  }

  /**
   * Check if a day in week view is a HOLIDAY
   */
  isWeekDayHoliday(date: Date): boolean {
    const dayData = this.getWeekDayData(date);
    // Check if calendarDayStatus is HOLIDAY (full day)
    return dayData?.calendarDayStatus === ("HOLIDAY" as any);
  }

  /**
   * Get description for a day in week view (for HOLIDAY days)
   */
  getWeekDayHolidayDescription(date: Date): string | null {
    // For HOLIDAY days, we need to get the description from the month data
    // since the week view doesn't contain the holiday description
    const monthDay = this.getDayFromBackend(date);
    if (monthDay?.description) {
      return monthDay.description;
    }

    return "Feriado";
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

  /**
   * Get display text for a slot based on its status and appointment data
   */
  getSlotDisplayText(slot: any): string {
    if (slot.status === "RESERVED" && slot.appointment) {
      return slot.appointment.patientName || "Cita reservada";
    }
    if (slot.status === "LOCKED") {
      return "Bloqueado";
    }
    return "Ocupado";
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
    const dialogRef = this.dialog.open(CreateAppointmentDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      data: {
        idDentist: this.personId, // Pasar el ID del dentista logueado
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Aquí se manejará la creación del turno con el día y slot seleccionados
        console.log("Turno a crear:", result);
        // TODO: Llamar al servicio de appointments para crear el turno
        // this.appointmentService.create(result);
      }
    });
  }

  goToAvailability() {
    this.router.navigate(["/dentist-availability/" + this.personId]); // Navegar a la ruta de disponibilidad
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
  }
}
