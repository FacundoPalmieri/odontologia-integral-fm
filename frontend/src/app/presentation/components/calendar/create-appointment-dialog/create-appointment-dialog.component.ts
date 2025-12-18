import { Component, inject, Inject, OnInit, signal } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatChipsModule } from "@angular/material/chips";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { CalendarService } from "../../../../services/calendar.service";
import { PatientService } from "../../../../services/patient.service";
import {
  CalendarDayInterface,
  CalendarWeekInterface,
  SlotInterface,
} from "../../../../domain/interfaces/calendar.interface";
import { PatientInterface } from "../../../../domain/interfaces/patient.interface";
import { PersonInterface } from "../../../../domain/interfaces/person.interface";
import { SlotStatusEnum } from "../../../../utils/enums/appointment/appointment-status.enum";
import { CommonModule } from "@angular/common";
import { debounceTime, distinctUntilChanged, switchMap, map } from "rxjs";
import { MatTooltipModule } from "@angular/material/tooltip";
import { AppointmentService } from "../../../../services/appointment.service";
import { AuthService } from "../../../../services/auth.service";
import { AppointmentInterface } from "../../../../domain/interfaces/appointment.inteface";
import { RequestSourceEnum } from "../../../../utils/enums/appointment/request-source.enum";

export interface CreateAppointmentDialogData {
  idDentist?: number; // Si existe, es un dentista creando su propio turno
}

@Component({
  selector: "app-create-appointment-dialog",
  templateUrl: "./create-appointment-dialog.component.html",
  styleUrls: ["./create-appointment-dialog.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    IconsModule,
  ],
})
export class CreateAppointmentDialogComponent implements OnInit {
  private readonly dialogRef = inject(
    MatDialogRef<CreateAppointmentDialogComponent>
  );
  private readonly calendarService = inject(CalendarService);
  private readonly patientService = inject(PatientService);
  private readonly appointmentService = inject(AppointmentService);
  private readonly authService = inject(AuthService);

  // Data recibida del diálogo
  idDentist?: number;

  // Estado de carga
  isLoadingWeek = signal(false);
  isLoadingSlots = signal(false);
  isSaving = signal(false);

  // Datos de la semana
  currentWeekStart = new Date();
  weekDays = signal<CalendarDayInterface[]>([]);
  currentMonthYear = "";

  // Día y slot seleccionados
  selectedDay = signal<CalendarDayInterface | null>(null);
  selectedSlot = signal<SlotInterface | null>(null);
  availableSlots = signal<SlotInterface[]>([]);

  // Buscador de pacientes
  patientSearchControl = new FormControl("");
  filteredPatients = signal<PatientInterface[]>([]);
  selectedPatient = signal<PatientInterface | null>(null);
  allPatients: PatientInterface[] = [];

  // Nombres de días en español
  private dayNames = ["Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"];
  private dayNamesComplete = [
    "Domingo",
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
  ];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: CreateAppointmentDialogData
  ) {
    this.idDentist = data?.idDentist;
  }

  ngOnInit(): void {
    // Cargar todos los pacientes
    this.loadPatients();

    // Configurar filtrado reactivo
    this.patientSearchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((searchTerm) => {
        this.filterPatients(searchTerm || "");
      });

    if (this.idDentist) {
      // Si hay idDentist, cargar la semana actual
      this.loadWeek(new Date());
    }
  }

  /**
   * Carga todos los pacientes del sistema
   */
  private loadPatients(): void {
    this.patientService.getAll(0, 10000, "person.lastName", "asc").subscribe({
      next: (response) => {
        if (response.data?.content) {
          // Convertir DTOs a interfaces (asumiendo que el DTO tiene la estructura correcta)
          this.allPatients = response.data
            .content as unknown as PatientInterface[];
        }
      },
      error: (error) => {
        console.error("Error al cargar pacientes:", error);
      },
    });
  }

  /**
   * Filtra pacientes por nombre, apellido o DNI
   * Solo filtra si hay 3 o más caracteres
   */
  private filterPatients(searchTerm: string): void {
    if (searchTerm.length < 3) {
      this.filteredPatients.set([]);
      return;
    }

    const term = searchTerm.toLowerCase();
    const filtered = this.allPatients.filter((patient) => {
      const firstName = patient.person.firstName.toLowerCase();
      const lastName = patient.person.lastName.toLowerCase();
      const dni = patient.person.dni.toLowerCase();

      return (
        firstName.includes(term) ||
        lastName.includes(term) ||
        dni.includes(term)
      );
    });

    this.filteredPatients.set(filtered);
  }

  /**
   * Maneja la selección de un paciente del autocomplete
   */
  onPatientSelected(patient: PatientInterface): void {
    this.selectedPatient.set(patient);
  }

  /**
   * Obtiene el texto a mostrar en el input del autocomplete
   */
  displayPatient(patient: PatientInterface | null): string {
    if (!patient) return "";
    return `${patient.person.firstName} ${patient.person.lastName} - DNI: ${patient.person.dni}`;
  }

  /**
   * Carga los datos de la semana desde el servicio
   */
  loadWeek(date: Date): void {
    if (!this.idDentist) return;

    this.isLoadingWeek.set(true);
    this.currentWeekStart = date;
    this.updateMonthYearLabel(date);

    this.calendarService.getWeek(this.idDentist, date).subscribe({
      next: (response) => {
        if (response.data) {
          // El backend siempre devuelve los días ordenados de domingo a sábado
          this.weekDays.set(response.data.days);

          // Seleccionar automáticamente el primer día disponible
          this.selectFirstAvailableDay();
        }
        this.isLoadingWeek.set(false);
      },
      error: (error) => {
        console.error("Error al cargar la semana:", error);
        this.isLoadingWeek.set(false);
      },
    });
  }

  /**
   * Navega a la semana anterior
   */
  previousWeek(): void {
    const newDate = new Date(this.currentWeekStart);
    newDate.setDate(newDate.getDate() - 7);
    this.loadWeek(newDate);
  }

  /**
   * Navega a la semana siguiente
   */
  nextWeek(): void {
    const newDate = new Date(this.currentWeekStart);
    newDate.setDate(newDate.getDate() + 7);
    this.loadWeek(newDate);
  }

  /**
   * Actualiza la etiqueta de mes y año
   */
  private updateMonthYearLabel(date: Date): void {
    const months = [
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
    this.currentMonthYear = `${months[date.getMonth()]} ${date.getFullYear()}`;
  }

  /**
   * Selecciona un día y carga sus slots disponibles
   */
  selectDay(day: CalendarDayInterface): void {
    if (!this.isDayAvailable(day)) return;

    this.selectedDay.set(day);
    this.selectedSlot.set(null);
    this.loadAvailableSlots(day);
  }

  /**
   * Carga los slots disponibles (FREE) del día seleccionado
   */
  private loadAvailableSlots(day: CalendarDayInterface): void {
    this.isLoadingSlots.set(true);

    // Filtrar solo los slots con status FREE
    const freeSlots = day.slots.filter(
      (slot) => slot.status === SlotStatusEnum.FREE
    );

    this.availableSlots.set(freeSlots);
    this.isLoadingSlots.set(false);
  }

  /**
   * Selecciona un slot
   */
  selectSlot(slot: SlotInterface): void {
    this.selectedSlot.set(slot);
  }

  /**
   * Verifica si un día está disponible (tiene al menos un slot FREE)
   */
  isDayAvailable(day: CalendarDayInterface): boolean {
    return day.slots.some((slot) => slot.status === SlotStatusEnum.FREE);
  }

  /**
   * Verifica si un día está seleccionado
   */
  isSelectedDay(day: CalendarDayInterface): boolean {
    const selected = this.selectedDay();
    if (!selected) return false;
    return this.isSameDay(selected.day, day.day);
  }

  /**
   * Verifica si un slot está seleccionado
   */
  isSelectedSlot(slot: SlotInterface): boolean {
    const selected = this.selectedSlot();
    if (!selected) return false;
    return (
      selected.starTime === slot.starTime && selected.endTime === slot.endTime
    );
  }

  /**
   * Compara si dos fechas son el mismo día
   */
  private isSameDay(date1: Date, date2: Date): boolean {
    const d1 = new Date(date1);
    const d2 = new Date(date2);
    return (
      d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate()
    );
  }

  /**
   * Obtiene el nombre del día (Lun, Mar, etc.)
   * Usa UTC para evitar problemas de zona horaria con fechas ISO del backend
   */
  getDayName(date: Date): string {
    const d = new Date(date);
    return this.dayNames[d.getUTCDay()];
  }

  /**
   * Obtiene el número del día
   * Usa UTC para evitar problemas de zona horaria con fechas ISO del backend
   */
  getDayNumber(date: Date): number {
    const d = new Date(date);
    return d.getUTCDate();
  }

  /**
   * Obtiene la etiqueta del día seleccionado con nombre completo
   */
  getSelectedDayLabel(): string {
    const day = this.selectedDay();
    if (!day) return "";

    const date = new Date(day.day);
    const dayName = this.dayNamesComplete[date.getUTCDay()];
    const dayNumber = this.getDayNumber(date);
    const month = date.toLocaleDateString("es-ES", { month: "long" });

    return `${dayName} ${dayNumber} de ${month}`;
  }

  /**
   * Limpia la selección de día y slot
   */
  private clearSelection(): void {
    this.selectedDay.set(null);
    this.selectedSlot.set(null);
    this.availableSlots.set([]);
  }

  /**
   * Selecciona automáticamente el primer día disponible de la semana
   */
  private selectFirstAvailableDay(): void {
    const days = this.weekDays();
    const firstAvailableDay = days.find((day) => this.isDayAvailable(day));

    if (firstAvailableDay) {
      this.selectDay(firstAvailableDay);
    }
  }

  /**
   * Formatea el tiempo eliminando los segundos (HH:MM:SS -> HH:MM)
   */
  formatTime(time: string): string {
    if (!time) return "";
    // Si el formato es HH:MM:SS, eliminar los segundos
    const parts = time.split(":");
    if (parts.length >= 2) {
      return `${parts[0]}:${parts[1]}`;
    }
    return time;
  }

  /**
   * Verifica si se puede guardar (hay día, slot y paciente seleccionados)
   */
  canSave(): boolean {
    return (
      this.selectedDay() !== null &&
      this.selectedSlot() !== null &&
      this.selectedPatient() !== null
    );
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (!this.canSave() || this.isSaving()) return;

    const userData = this.authService.getUserData();
    if (!userData || !userData.person) {
      console.error("No se pudo obtener la información del usuario");
      return;
    }

    // Convertir PersonDtoInterface a PersonInterface
    const dentistPerson: PersonInterface = {
      id: userData.person.id,
      firstName: userData.person.firstName,
      lastName: userData.person.lastName,
      dniType: { id: 0, dni: userData.person.dniType },
      dni: userData.person.dni,
      birthDate: userData.person.birthDate,
      gender: {
        id: 0,
        alias: userData.person.gender,
        name: userData.person.gender,
      },
      nationality: { id: 0, name: userData.person.nationality }, // Simplificado
      contactEmails: userData.person.contactEmails.join(", "), // Convertir array a string
      phoneType: {
        id: 0,
        name: userData.person.contactPhone[0]?.typePhone || "",
      },
      phone: userData.person.contactPhone[0]?.phone || "",
      country: {
        id: userData.person.address.countryId,
        name: userData.person.address.country,
      },
      province: {
        id: userData.person.address.provinceId,
        name: userData.person.address.province,
      },
      locality: {
        id: userData.person.address.localityId,
        name: userData.person.address.locality,
      },
      street: userData.person.address.street,
      number: userData.person.address.number,
      floor: userData.person.address.floor,
      apartment: userData.person.address.apartment,
    };

    // Construir el objeto AppointmentInterface
    const dateTime = this.buildDateTime(
      this.selectedDay()!.day,
      this.selectedSlot()!.starTime
    );

    const appointment: AppointmentInterface = {
      patient: this.selectedPatient()!,
      dentist: dentistPerson,
      dateTime: dateTime,
      requestSource: RequestSourceEnum.DENTIST,
    };

    // Crear el appointment
    this.isSaving.set(true);
    this.appointmentService.create(appointment).subscribe({
      next: (response) => {
        this.isSaving.set(false);
        console.log("Appointment creado exitosamente:", response);
        this.dialogRef.close({
          success: true,
          data: response.data,
        });
      },
      error: (error) => {
        this.isSaving.set(false);
        console.error("Error al crear el appointment:", error);
        // Aquí podrías mostrar un mensaje de error al usuario
        this.dialogRef.close({
          success: false,
          error: error,
        });
      },
    });
  }

  /**
   * Construye un objeto Date combinando el día seleccionado con la hora del slot
   * Usa UTC para evitar problemas de zona horaria
   */
  private buildDateTime(day: Date, time: string): Date {
    // Obtener año, mes y día en UTC del día seleccionado
    const dayDate = new Date(day);
    const year = dayDate.getUTCFullYear();
    const month = dayDate.getUTCMonth();
    const dayOfMonth = dayDate.getUTCDate();

    // Extraer horas y minutos del tiempo (formato HH:MM:SS)
    const [hours, minutes] = time.split(":").map(Number);

    // Crear una nueva fecha con año, mes, día y hora en la zona horaria local
    // Esto evita problemas de conversión UTC
    const dateTime = new Date(year, month, dayOfMonth, hours, minutes, 0, 0);

    return dateTime;
  }
}
