import {
  Component,
  inject,
  OnInit,
  AfterViewInit,
  ElementRef,
  ViewChild,
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
import { MatDialogModule } from "@angular/material/dialog";
import { MatTooltipModule } from "@angular/material/tooltip";
import { LoaderService } from "../../../services/loader.service";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";

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
  ],
})
export class CalendarComponent implements OnInit, AfterViewInit {
  loaderService = inject(LoaderService);
  loading$ = this.loaderService.loading$;

  @ViewChild("timeColumn", { static: false }) timeColumn!: ElementRef;
  @ViewChild("eventsColumn", { static: false }) eventsColumn!: ElementRef;

  private scrollHandler: (() => void) | null = null;

  currentDate = new Date();
  selectedDate = new Date();
  currentView: CalendarView = "month";

  // Configuración de horarios de trabajo
  workStartHour = 0; // 12:00 AM (medianoche)
  workEndHour = 24; // 12:00 AM (medianoche del día siguiente)

  // Eventos de ejemplo
  events: CalendarEvent[] = [
    {
      id: "1",
      title: "Consulta - Juan Pérez",
      start: new Date(2024, 11, 15, 9, 0),
      end: new Date(2024, 11, 15, 9, 30),
      color: "#3f51b5",
      description: "Consulta de rutina",
    },
    {
      id: "2",
      title: "Limpieza - María García",
      start: new Date(2024, 11, 15, 10, 0),
      end: new Date(2024, 11, 15, 11, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "3",
      title: "Extracción - Carlos López",
      start: new Date(2024, 11, 15, 14, 30),
      end: new Date(2024, 11, 15, 15, 30),
      color: "#f44336",
      description: "Extracción de muela",
    },
    {
      id: "4",
      title: "Revisión - Ana Martínez",
      start: new Date(2024, 11, 15, 16, 0),
      end: new Date(2024, 11, 15, 16, 30),
      color: "#ff9800",
      description: "Revisión post-tratamiento",
    },
    {
      id: "5",
      title: "Emergencia - Roberto Silva",
      start: new Date(2024, 11, 15, 20, 0),
      end: new Date(2024, 11, 15, 21, 0),
      color: "#e91e63",
      description: "Emergencia dental nocturna",
    },
    {
      id: "6",
      title: "Guardia - Dr. García",
      start: new Date(2024, 11, 15, 22, 0),
      end: new Date(2024, 11, 15, 23, 30),
      color: "#9c27b0",
      description: "Guardia nocturna",
    },
  ];

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
    this.updateSelectedDate();
  }

  ngAfterViewInit() {
    this.setupScrollSync();
    this.startTimeUpdate();
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
  }

  goToToday() {
    this.selectedDate = new Date();
    this.updateSelectedDate();
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

    // Generar 42 días (6 semanas)
    for (let i = 0; i < 42; i++) {
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

      return (
        eventStart.toDateString() === date.toDateString() &&
        ((eventStart >= slotStart && eventStart < slotEnd) ||
          (eventEnd > slotStart && eventEnd <= slotEnd) ||
          (eventStart <= slotStart && eventEnd >= slotEnd))
      );
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
  }

  selectDate(date: Date) {
    this.selectedDate = date;
  }

  onEventClick(event: CalendarEvent) {
    console.log("Event clicked:", event);
    // Aquí puedes abrir un diálogo o navegar a los detalles del evento
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

    // Actualizar cada minuto
    setInterval(() => {
      this.updateCurrentTime();
    }, 60000); // 60 segundos
  }

  // Método para forzar la actualización de la línea de tiempo
  updateCurrentTime() {
    // Forzar la detección de cambios
    if (this.isCurrentDay()) {
      // La línea se actualizará automáticamente por el binding
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

  createEvent() {}
}
