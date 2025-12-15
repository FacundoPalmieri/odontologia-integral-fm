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

  // Cache para almacenar los meses ya cargados (key: "YYYY-MM", value: CalendarMonthInterface)
  private monthCache = new Map<string, CalendarMonthInterface>();

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
  events: CalendarEvent[] = [
    // OCTUBRE 2025 - Semana 1 (1-5 octubre)
    {
      id: "1",
      title: "Consulta - Juan Pérez",
      start: new Date(2025, 9, 1, 9, 0),
      end: new Date(2025, 9, 1, 9, 30),
      color: "#3f51b5",
      description: "Consulta de rutina",
    },
    {
      id: "2",
      title: "Limpieza - María García",
      start: new Date(2025, 9, 1, 10, 0),
      end: new Date(2025, 9, 1, 11, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "3",
      title: "Ortodoncia - Sofía Rodríguez",
      start: new Date(2025, 9, 2, 8, 30),
      end: new Date(2025, 9, 2, 9, 30),
      color: "#2196f3",
      description: "Ajuste de brackets",
    },
    {
      id: "4",
      title: "Endodoncia - Miguel Torres",
      start: new Date(2025, 9, 2, 10, 0),
      end: new Date(2025, 9, 2, 12, 0),
      color: "#795548",
      description: "Tratamiento de conducto",
    },
    {
      id: "5",
      title: "Prótesis - Carmen Vega",
      start: new Date(2025, 9, 3, 14, 0),
      end: new Date(2025, 9, 3, 15, 30),
      color: "#607d8b",
      description: "Colocación de prótesis",
    },
    {
      id: "6",
      title: "Cirugía - Diego Morales",
      start: new Date(2025, 9, 3, 16, 0),
      end: new Date(2025, 9, 3, 18, 0),
      color: "#d32f2f",
      description: "Extracción de cordal",
    },
    {
      id: "7",
      title: "Consulta - Laura Jiménez",
      start: new Date(2025, 9, 4, 9, 0),
      end: new Date(2025, 9, 4, 9, 30),
      color: "#3f51b5",
      description: "Primera consulta",
    },
    {
      id: "8",
      title: "Limpieza - Fernando Ruiz",
      start: new Date(2025, 9, 4, 10, 30),
      end: new Date(2025, 9, 4, 11, 30),
      color: "#4caf50",
      description: "Limpieza profunda",
    },
    {
      id: "9",
      title: "Blanqueamiento - Patricia López",
      start: new Date(2025, 9, 4, 15, 0),
      end: new Date(2025, 9, 4, 16, 30),
      color: "#ffc107",
      description: "Tratamiento blanqueador",
    },
    {
      id: "10",
      title: "Emergencia - Luis González",
      start: new Date(2025, 9, 5, 8, 0),
      end: new Date(2025, 9, 5, 9, 0),
      color: "#e91e63",
      description: "Dolor agudo",
    },

    // OCTUBRE 2025 - Semana 2 (6-12 octubre)
    {
      id: "11",
      title: "Ortodoncia - Alejandro Castro",
      start: new Date(2025, 9, 6, 8, 0),
      end: new Date(2025, 9, 6, 9, 0),
      color: "#2196f3",
      description: "Control mensual",
    },
    {
      id: "12",
      title: "Implante - Rosa Mendoza",
      start: new Date(2025, 9, 6, 10, 0),
      end: new Date(2025, 9, 6, 12, 30),
      color: "#9c27b0",
      description: "Colocación de implante",
    },
    {
      id: "13",
      title: "Consulta - Eduardo Vargas",
      start: new Date(2025, 9, 7, 14, 0),
      end: new Date(2025, 9, 7, 14, 30),
      color: "#3f51b5",
      description: "Consulta de seguimiento",
    },
    {
      id: "14",
      title: "Periodoncia - Isabel Herrera",
      start: new Date(2025, 9, 7, 15, 0),
      end: new Date(2025, 9, 7, 16, 0),
      color: "#ff5722",
      description: "Tratamiento periodontal",
    },
    {
      id: "15",
      title: "Revisión - Mónica Silva",
      start: new Date(2025, 9, 8, 10, 0),
      end: new Date(2025, 9, 8, 10, 30),
      color: "#ff9800",
      description: "Revisión post-cirugía",
    },
    {
      id: "16",
      title: "Prótesis - Ricardo Peña",
      start: new Date(2025, 9, 8, 11, 0),
      end: new Date(2025, 9, 8, 12, 30),
      color: "#607d8b",
      description: "Prueba de prótesis",
    },
    {
      id: "17",
      title: "Limpieza - Gabriela Flores",
      start: new Date(2025, 9, 9, 9, 0),
      end: new Date(2025, 9, 9, 10, 0),
      color: "#4caf50",
      description: "Limpieza rutinaria",
    },
    {
      id: "18",
      title: "Ortodoncia - Sebastián Cruz",
      start: new Date(2025, 9, 9, 11, 0),
      end: new Date(2025, 9, 9, 12, 0),
      color: "#2196f3",
      description: "Cambio de ligaduras",
    },
    {
      id: "19",
      title: "Endodoncia - Valeria Ramos",
      start: new Date(2025, 9, 10, 14, 0),
      end: new Date(2025, 9, 10, 16, 0),
      color: "#795548",
      description: "Finalización de conducto",
    },
    {
      id: "20",
      title: "Consulta - Andrés Moreno",
      start: new Date(2025, 9, 11, 8, 30),
      end: new Date(2025, 9, 11, 9, 0),
      color: "#3f51b5",
      description: "Consulta inicial",
    },
    {
      id: "21",
      title: "Cirugía - Claudia Rojas",
      start: new Date(2025, 9, 11, 10, 0),
      end: new Date(2025, 9, 11, 12, 0),
      color: "#d32f2f",
      description: "Extracción múltiple",
    },
    {
      id: "22",
      title: "Blanqueamiento - Hugo Sánchez",
      start: new Date(2025, 9, 12, 15, 0),
      end: new Date(2025, 9, 12, 16, 0),
      color: "#ffc107",
      description: "Sesión de blanqueamiento",
    },

    // OCTUBRE 2025 - Semana 3 (13-19 octubre)
    {
      id: "23",
      title: "Implante - Adriana Castillo",
      start: new Date(2025, 9, 13, 9, 0),
      end: new Date(2025, 9, 13, 11, 30),
      color: "#9c27b0",
      description: "Cirugía de implante",
    },
    {
      id: "24",
      title: "Periodoncia - Omar Delgado",
      start: new Date(2025, 9, 13, 14, 0),
      end: new Date(2025, 9, 13, 15, 30),
      color: "#ff5722",
      description: "Mantenimiento periodontal",
    },
    {
      id: "25",
      title: "Revisión - Natalia Espinoza",
      start: new Date(2025, 9, 14, 9, 0),
      end: new Date(2025, 9, 14, 9, 30),
      color: "#ff9800",
      description: "Control post-tratamiento",
    },
    {
      id: "26",
      title: "Prótesis - Carlos Mendoza",
      start: new Date(2025, 9, 14, 10, 0),
      end: new Date(2025, 9, 14, 12, 0),
      color: "#607d8b",
      description: "Instalación de prótesis",
    },
    {
      id: "27",
      title: "Emergencia - Dr. García",
      start: new Date(2025, 9, 15, 9, 0),
      end: new Date(2025, 9, 15, 12, 0),
      color: "#e91e63",
      description: "Guardia de emergencias",
    },
    {
      id: "28",
      title: "Consulta - Dr. García",
      start: new Date(2025, 9, 16, 9, 0),
      end: new Date(2025, 9, 16, 17, 0),
      color: "#3f51b5",
      description: "Horario normal",
    },
    {
      id: "29",
      title: "Limpieza - Ana Beltrán",
      start: new Date(2025, 9, 17, 9, 0),
      end: new Date(2025, 9, 17, 10, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "30",
      title: "Ortodoncia - Roberto Vega",
      start: new Date(2025, 9, 17, 11, 0),
      end: new Date(2025, 9, 17, 12, 0),
      color: "#2196f3",
      description: "Control ortodoncia",
    },
    {
      id: "31",
      title: "Endodoncia - Silvia Torres",
      start: new Date(2025, 9, 18, 14, 0),
      end: new Date(2025, 9, 18, 16, 0),
      color: "#795548",
      description: "Tratamiento de conducto",
    },
    {
      id: "32",
      title: "Cirugía - Manuel Herrera",
      start: new Date(2025, 9, 19, 8, 0),
      end: new Date(2025, 9, 19, 10, 30),
      color: "#d32f2f",
      description: "Extracción quirúrgica",
    },

    // OCTUBRE 2025 - Semana 4 (20-26 octubre)
    {
      id: "33",
      title: "Implante - Elena Morales",
      start: new Date(2025, 9, 20, 11, 0),
      end: new Date(2025, 9, 20, 13, 0),
      color: "#9c27b0",
      description: "Colocación de implante",
    },
    {
      id: "34",
      title: "Revisión - Jorge Ramos",
      start: new Date(2025, 9, 20, 15, 0),
      end: new Date(2025, 9, 20, 15, 30),
      color: "#ff9800",
      description: "Control post-cirugía",
    },
    {
      id: "35",
      title: "Blanqueamiento - Carmen Ruiz",
      start: new Date(2025, 9, 21, 9, 0),
      end: new Date(2025, 9, 21, 11, 0),
      color: "#ffc107",
      description: "Tratamiento blanqueador",
    },
    {
      id: "36",
      title: "Periodoncia - Francisco López",
      start: new Date(2025, 9, 21, 14, 0),
      end: new Date(2025, 9, 21, 15, 30),
      color: "#ff5722",
      description: "Tratamiento periodontal",
    },
    {
      id: "37",
      title: "Prótesis - Beatriz Castro",
      start: new Date(2025, 9, 22, 9, 0),
      end: new Date(2025, 9, 22, 11, 30),
      color: "#607d8b",
      description: "Instalación de prótesis",
    },
    {
      id: "38",
      title: "Consulta - Raúl Jiménez",
      start: new Date(2025, 9, 22, 14, 0),
      end: new Date(2025, 9, 22, 14, 30),
      color: "#3f51b5",
      description: "Consulta de rutina",
    },
    {
      id: "39",
      title: "Emergencias - Dr. García",
      start: new Date(2025, 9, 23, 9, 0),
      end: new Date(2025, 9, 23, 13, 0),
      color: "#e91e63",
      description: "Guardia de emergencias",
    },
    {
      id: "40",
      title: "Consulta - María González",
      start: new Date(2025, 9, 24, 9, 0),
      end: new Date(2025, 9, 24, 9, 30),
      color: "#3f51b5",
      description: "Primera consulta del mes",
    },
    {
      id: "41",
      title: "Limpieza - Pedro Martínez",
      start: new Date(2025, 9, 24, 10, 0),
      end: new Date(2025, 9, 24, 11, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "42",
      title: "Ortodoncia - Lucía Fernández",
      start: new Date(2025, 9, 25, 8, 30),
      end: new Date(2025, 9, 25, 9, 30),
      color: "#2196f3",
      description: "Control ortodoncia",
    },

    // OCTUBRE 2025 - Semana 5 (27-31 octubre)
    {
      id: "43",
      title: "Endodoncia - Antonio Ruiz",
      start: new Date(2025, 9, 27, 10, 0),
      end: new Date(2025, 9, 27, 12, 0),
      color: "#795548",
      description: "Tratamiento de conducto",
    },
    {
      id: "44",
      title: "Cirugía - Isabel Sánchez",
      start: new Date(2025, 9, 28, 9, 0),
      end: new Date(2025, 9, 28, 11, 0),
      color: "#d32f2f",
      description: "Extracción de cordal",
    },
    {
      id: "45",
      title: "Consulta - Juan Pérez",
      start: new Date(2025, 9, 29, 9, 0),
      end: new Date(2025, 9, 29, 9, 30),
      color: "#3f51b5",
      description: "Consulta de seguimiento",
    },
    {
      id: "46",
      title: "Limpieza - María García",
      start: new Date(2025, 9, 29, 10, 0),
      end: new Date(2025, 9, 29, 11, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "47",
      title: "Emergencia - Roberto Silva",
      start: new Date(2025, 9, 30, 20, 0),
      end: new Date(2025, 9, 30, 21, 0),
      color: "#e91e63",
      description: "Emergencia dental nocturna",
    },
    {
      id: "48",
      title: "Guardia - Dr. García",
      start: new Date(2025, 9, 31, 22, 0),
      end: new Date(2025, 9, 31, 23, 30),
      color: "#9c27b0",
      description: "Guardia nocturna",
    },

    // NOVIEMBRE 2025 - Semana 1 (1-5 noviembre)
    {
      id: "49",
      title: "Consulta - Juan Pérez",
      start: new Date(2025, 10, 1, 9, 0),
      end: new Date(2025, 10, 1, 9, 30),
      color: "#3f51b5",
      description: "Consulta de rutina",
    },
    {
      id: "50",
      title: "Limpieza - María García",
      start: new Date(2025, 10, 1, 10, 0),
      end: new Date(2025, 10, 1, 11, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "51",
      title: "Extracción - Carlos López",
      start: new Date(2025, 10, 1, 14, 30),
      end: new Date(2025, 10, 1, 15, 30),
      color: "#f44336",
      description: "Extracción de muela",
    },
    {
      id: "52",
      title: "Revisión - Ana Martínez",
      start: new Date(2025, 10, 1, 16, 0),
      end: new Date(2025, 10, 1, 16, 30),
      color: "#ff9800",
      description: "Revisión post-tratamiento",
    },
    {
      id: "53",
      title: "Ortodoncia - Sofía Rodríguez",
      start: new Date(2025, 10, 2, 8, 30),
      end: new Date(2025, 10, 2, 9, 30),
      color: "#2196f3",
      description: "Ajuste de brackets",
    },
    {
      id: "54",
      title: "Endodoncia - Miguel Torres",
      start: new Date(2025, 10, 2, 10, 0),
      end: new Date(2025, 10, 2, 12, 0),
      color: "#795548",
      description: "Tratamiento de conducto",
    },
    {
      id: "55",
      title: "Prótesis - Carmen Vega",
      start: new Date(2025, 10, 3, 14, 0),
      end: new Date(2025, 10, 3, 15, 30),
      color: "#607d8b",
      description: "Colocación de prótesis",
    },
    {
      id: "56",
      title: "Cirugía - Diego Morales",
      start: new Date(2025, 10, 3, 16, 0),
      end: new Date(2025, 10, 3, 18, 0),
      color: "#d32f2f",
      description: "Extracción de cordal",
    },
    {
      id: "57",
      title: "Consulta - Laura Jiménez",
      start: new Date(2025, 10, 4, 9, 0),
      end: new Date(2025, 10, 4, 9, 30),
      color: "#3f51b5",
      description: "Primera consulta",
    },
    {
      id: "58",
      title: "Limpieza - Fernando Ruiz",
      start: new Date(2025, 10, 4, 10, 30),
      end: new Date(2025, 10, 4, 11, 30),
      color: "#4caf50",
      description: "Limpieza profunda",
    },
    {
      id: "59",
      title: "Blanqueamiento - Patricia López",
      start: new Date(2025, 10, 4, 15, 0),
      end: new Date(2025, 10, 4, 16, 30),
      color: "#ffc107",
      description: "Tratamiento blanqueador",
    },
    {
      id: "60",
      title: "Emergencia - Luis González",
      start: new Date(2025, 10, 5, 8, 0),
      end: new Date(2025, 10, 5, 9, 0),
      color: "#e91e63",
      description: "Dolor agudo",
    },

    // NOVIEMBRE 2025 - Semana 2 (6-12 noviembre)
    {
      id: "61",
      title: "Ortodoncia - Alejandro Castro",
      start: new Date(2025, 10, 6, 8, 0),
      end: new Date(2025, 10, 6, 9, 0),
      color: "#2196f3",
      description: "Control mensual",
    },
    {
      id: "62",
      title: "Implante - Rosa Mendoza",
      start: new Date(2025, 10, 6, 10, 0),
      end: new Date(2025, 10, 6, 12, 30),
      color: "#9c27b0",
      description: "Colocación de implante",
    },
    {
      id: "63",
      title: "Consulta - Eduardo Vargas",
      start: new Date(2025, 10, 7, 14, 0),
      end: new Date(2025, 10, 7, 14, 30),
      color: "#3f51b5",
      description: "Consulta de seguimiento",
    },
    {
      id: "64",
      title: "Periodoncia - Isabel Herrera",
      start: new Date(2025, 10, 7, 15, 0),
      end: new Date(2025, 10, 7, 16, 0),
      color: "#ff5722",
      description: "Tratamiento periodontal",
    },
    {
      id: "65",
      title: "Revisión - Mónica Silva",
      start: new Date(2025, 10, 8, 10, 0),
      end: new Date(2025, 10, 8, 10, 30),
      color: "#ff9800",
      description: "Revisión post-cirugía",
    },
    {
      id: "66",
      title: "Prótesis - Ricardo Peña",
      start: new Date(2025, 10, 8, 11, 0),
      end: new Date(2025, 10, 8, 12, 30),
      color: "#607d8b",
      description: "Prueba de prótesis",
    },
    {
      id: "67",
      title: "Limpieza - Gabriela Flores",
      start: new Date(2025, 10, 9, 9, 0),
      end: new Date(2025, 10, 9, 10, 0),
      color: "#4caf50",
      description: "Limpieza rutinaria",
    },
    {
      id: "68",
      title: "Ortodoncia - Sebastián Cruz",
      start: new Date(2025, 10, 9, 11, 0),
      end: new Date(2025, 10, 9, 12, 0),
      color: "#2196f3",
      description: "Cambio de ligaduras",
    },
    {
      id: "69",
      title: "Endodoncia - Valeria Ramos",
      start: new Date(2025, 10, 10, 14, 0),
      end: new Date(2025, 10, 10, 16, 0),
      color: "#795548",
      description: "Finalización de conducto",
    },
    {
      id: "70",
      title: "Consulta - Andrés Moreno",
      start: new Date(2025, 10, 11, 8, 30),
      end: new Date(2025, 10, 11, 9, 0),
      color: "#3f51b5",
      description: "Consulta inicial",
    },
    {
      id: "71",
      title: "Cirugía - Claudia Rojas",
      start: new Date(2025, 10, 11, 10, 0),
      end: new Date(2025, 10, 11, 12, 0),
      color: "#d32f2f",
      description: "Extracción múltiple",
    },
    {
      id: "72",
      title: "Blanqueamiento - Hugo Sánchez",
      start: new Date(2025, 10, 12, 15, 0),
      end: new Date(2025, 10, 12, 16, 0),
      color: "#ffc107",
      description: "Sesión de blanqueamiento",
    },

    // NOVIEMBRE 2025 - Semana 3 (13-19 noviembre)
    {
      id: "73",
      title: "Implante - Adriana Castillo",
      start: new Date(2025, 10, 13, 9, 0),
      end: new Date(2025, 10, 13, 11, 30),
      color: "#9c27b0",
      description: "Cirugía de implante",
    },
    {
      id: "74",
      title: "Periodoncia - Omar Delgado",
      start: new Date(2025, 10, 13, 14, 0),
      end: new Date(2025, 10, 13, 15, 30),
      color: "#ff5722",
      description: "Mantenimiento periodontal",
    },
    {
      id: "75",
      title: "Revisión - Natalia Espinoza",
      start: new Date(2025, 10, 14, 9, 0),
      end: new Date(2025, 10, 14, 9, 30),
      color: "#ff9800",
      description: "Control post-tratamiento",
    },
    {
      id: "76",
      title: "Prótesis - Carlos Mendoza",
      start: new Date(2025, 10, 14, 10, 0),
      end: new Date(2025, 10, 14, 12, 0),
      color: "#607d8b",
      description: "Instalación de prótesis",
    },
    {
      id: "77",
      title: "Emergencia - Dr. García",
      start: new Date(2025, 10, 15, 9, 0),
      end: new Date(2025, 10, 15, 12, 0),
      color: "#e91e63",
      description: "Guardia de emergencias",
    },
    {
      id: "78",
      title: "Consulta - Dr. García",
      start: new Date(2025, 10, 16, 9, 0),
      end: new Date(2025, 10, 16, 17, 0),
      color: "#3f51b5",
      description: "Horario normal",
    },
    {
      id: "79",
      title: "Limpieza - Ana Beltrán",
      start: new Date(2025, 10, 17, 9, 0),
      end: new Date(2025, 10, 17, 10, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "80",
      title: "Ortodoncia - Roberto Vega",
      start: new Date(2025, 10, 17, 11, 0),
      end: new Date(2025, 10, 17, 12, 0),
      color: "#2196f3",
      description: "Control ortodoncia",
    },
    {
      id: "81",
      title: "Endodoncia - Silvia Torres",
      start: new Date(2025, 10, 18, 14, 0),
      end: new Date(2025, 10, 18, 16, 0),
      color: "#795548",
      description: "Tratamiento de conducto",
    },
    {
      id: "82",
      title: "Cirugía - Manuel Herrera",
      start: new Date(2025, 10, 19, 8, 0),
      end: new Date(2025, 10, 19, 10, 30),
      color: "#d32f2f",
      description: "Extracción quirúrgica",
    },

    // NOVIEMBRE 2025 - Semana 4 (20-26 noviembre)
    {
      id: "83",
      title: "Implante - Elena Morales",
      start: new Date(2025, 10, 20, 11, 0),
      end: new Date(2025, 10, 20, 13, 0),
      color: "#9c27b0",
      description: "Colocación de implante",
    },
    {
      id: "84",
      title: "Revisión - Jorge Ramos",
      start: new Date(2025, 10, 20, 15, 0),
      end: new Date(2025, 10, 20, 15, 30),
      color: "#ff9800",
      description: "Control post-cirugía",
    },
    {
      id: "85",
      title: "Blanqueamiento - Carmen Ruiz",
      start: new Date(2025, 10, 21, 9, 0),
      end: new Date(2025, 10, 21, 11, 0),
      color: "#ffc107",
      description: "Tratamiento blanqueador",
    },
    {
      id: "86",
      title: "Periodoncia - Francisco López",
      start: new Date(2025, 10, 21, 14, 0),
      end: new Date(2025, 10, 21, 15, 30),
      color: "#ff5722",
      description: "Tratamiento periodontal",
    },
    {
      id: "87",
      title: "Prótesis - Beatriz Castro",
      start: new Date(2025, 10, 22, 9, 0),
      end: new Date(2025, 10, 22, 11, 30),
      color: "#607d8b",
      description: "Instalación de prótesis",
    },
    {
      id: "88",
      title: "Consulta - Raúl Jiménez",
      start: new Date(2025, 10, 22, 14, 0),
      end: new Date(2025, 10, 22, 14, 30),
      color: "#3f51b5",
      description: "Consulta de rutina",
    },
    {
      id: "89",
      title: "Emergencias - Dr. García",
      start: new Date(2025, 10, 23, 9, 0),
      end: new Date(2025, 10, 23, 13, 0),
      color: "#e91e63",
      description: "Guardia de emergencias",
    },
    {
      id: "90",
      title: "Consulta - María González",
      start: new Date(2025, 10, 24, 9, 0),
      end: new Date(2025, 10, 24, 9, 30),
      color: "#3f51b5",
      description: "Primera consulta del mes",
    },
    {
      id: "91",
      title: "Limpieza - Pedro Martínez",
      start: new Date(2025, 10, 24, 10, 0),
      end: new Date(2025, 10, 24, 11, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "92",
      title: "Ortodoncia - Lucía Fernández",
      start: new Date(2025, 10, 25, 8, 30),
      end: new Date(2025, 10, 25, 9, 30),
      color: "#2196f3",
      description: "Control ortodoncia",
    },

    // NOVIEMBRE 2025 - Semana 5 (27-30 noviembre)
    {
      id: "93",
      title: "Endodoncia - Antonio Ruiz",
      start: new Date(2025, 10, 27, 10, 0),
      end: new Date(2025, 10, 27, 12, 0),
      color: "#795548",
      description: "Tratamiento de conducto",
    },
    {
      id: "94",
      title: "Cirugía - Isabel Sánchez",
      start: new Date(2025, 10, 28, 9, 0),
      end: new Date(2025, 10, 28, 11, 0),
      color: "#d32f2f",
      description: "Extracción de cordal",
    },
    {
      id: "95",
      title: "Consulta - Juan Pérez",
      start: new Date(2025, 10, 29, 9, 0),
      end: new Date(2025, 10, 29, 9, 30),
      color: "#3f51b5",
      description: "Consulta de seguimiento",
    },
    {
      id: "96",
      title: "Limpieza - María García",
      start: new Date(2025, 10, 29, 10, 0),
      end: new Date(2025, 10, 29, 11, 0),
      color: "#4caf50",
      description: "Limpieza dental",
    },
    {
      id: "97",
      title: "Emergencia - Roberto Silva",
      start: new Date(2025, 10, 30, 20, 0),
      end: new Date(2025, 10, 30, 21, 0),
      color: "#e91e63",
      description: "Emergencia dental nocturna",
    },
    {
      id: "98",
      title: "Guardia - Dr. García",
      start: new Date(2025, 10, 30, 22, 0),
      end: new Date(2025, 10, 30, 23, 30),
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

  generateTimeSlots() {
    // Generar horarios de trabajo configurables (cada hora)
    for (let hour = this.workStartHour; hour < this.workEndHour; hour++) {
      const timeString = `${hour.toString().padStart(2, "0")}:00`;
      this.timeSlots.push(timeString);
    }
  }

  setView(view: CalendarView) {
    this.currentView = view;

    // Load data when switching to month view
    if (view === "month") {
      this.loadMonthView();
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

    // Reload data when navigating in month view
    if (this.currentView === "month") {
      this.loadMonthView();
    }
  }

  goToToday() {
    this.selectedDate = new Date();
    this.updateSelectedDate();

    // Reload data when going to today in month view
    if (this.currentView === "month") {
      this.loadMonthView();
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
   * Get the description for a specific day (only for holidays and full days)
   */
  getDayDescription(date: Date): string | null {
    const dayData = this.getDayFromBackend(date);
    // Mostrar descripción si es feriado o día completo
    if (
      dayData?.status === CalendarMonthDayStatusEnum.HOLIDAY ||
      dayData?.status === CalendarMonthDayStatusEnum.FULL
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
      return "⛔"; // Candado para días completos/bloqueados
    }

    return "📅"; // Calendario por defecto
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
      width: "600px",
      data: {
        selectedDate: this.selectedDate,
        selectedTime: this.getCurrentTimeSlot(),
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Here you can handle the appointment creation
        // For example: this.appointmentService.create(result);
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
