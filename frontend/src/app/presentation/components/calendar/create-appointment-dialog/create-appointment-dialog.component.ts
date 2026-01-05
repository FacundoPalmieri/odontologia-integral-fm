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
import { DentistService } from "../../../../services/dentist.service";
import {
  CalendarDayInterface,
  CalendarWeekInterface,
  SlotInterface,
} from "../../../../domain/interfaces/calendar.interface";
import { PatientInterface } from "../../../../domain/interfaces/patient.interface";
import { PersonInterface } from "../../../../domain/interfaces/person.interface";
import { SlotStatusEnum } from "../../../../utils/enums/appointment/appointment-status.enum";
import { CommonModule } from "@angular/common";
import { debounceTime, distinctUntilChanged, map, forkJoin } from "rxjs";
import { MatTooltipModule } from "@angular/material/tooltip";
import { AppointmentService } from "../../../../services/appointment.service";
import { AuthService } from "../../../../services/auth.service";
import { AppointmentInterface } from "../../../../domain/interfaces/appointment.inteface";
import { RequestSourceEnum } from "../../../../utils/enums/appointment/request-source.enum";
import { RoleEnum } from "../../../../utils/enums/role.enum";
import { DentistDtoInterface } from "../../../../domain/dto/dentist.dto";

// Interfaz para agrupar dentistas por especialidad
interface SpecialtyGroup {
  specialtyName: string;
  dentists: DentistDtoInterface[];
}

// Interfaz para rastrear disponibilidad de dentistas
interface DentistAvailability {
  dentist: DentistDtoInterface;
  weekData: CalendarWeekInterface | null;
}

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
  private readonly dentistService = inject(DentistService);

  // Data recibida del diálogo
  idDentist?: number;

  // Estado de carga
  isLoadingWeek = signal(false);
  isLoadingSlots = signal(false);
  isSaving = signal(false);

  // Datos de la semana
  currentWeekStart = new Date();
  currentWeekStartForSecretary = new Date();
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

  // ===== PROPIEDADES PARA VISTA DE SECRETARIO/ADMINISTRADOR =====
  // Especialidades agrupadas
  specialtyGroups = signal<SpecialtyGroup[]>([]);
  selectedSpecialty = signal<string | null>(null);

  // Disponibilidad de dentistas por especialidad
  dentistAvailabilities = signal<DentistAvailability[]>([]);
  isLoadingDentistWeeks = signal(false);

  // Dentista seleccionado
  selectedDentist = signal<DentistDtoInterface | null>(null);
  availableDentistsForDay = signal<DentistDtoInterface[]>([]);

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
    // Obtener el rol del usuario
    const userRole = this.authService.getUserRole();

    // Cargar todos los pacientes
    this.loadPatients();

    // Configurar filtrado reactivo
    this.patientSearchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((searchTerm) => {
        this.filterPatients(searchTerm || "");
      });

    if (userRole === RoleEnum.DENTIST) {
      // Si es dentista, cargar la semana actual
      if (this.idDentist) {
        this.loadWeek(new Date());
      }
    } else if (
      userRole === RoleEnum.SECRETARY ||
      userRole === RoleEnum.ADMINISTRATOR
    ) {
      // Si es secretaria o administrador, cargar todos los dentistas
      this.dentistService.getAll().subscribe({
        next: (response) => {
          if (response.data) {
            // Agrupar dentistas por especialidad
            this.groupDentistsBySpecialty(response.data);
          }
        },
        error: (error) => {
          console.error("Error al cargar dentistas:", error);
        },
      });
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
    if (this.idDentist) {
      // Vista de dentista
      const newDate = new Date(this.currentWeekStart);
      newDate.setDate(newDate.getDate() - 7);
      this.loadWeek(newDate);
    } else {
      // Vista de secretario/administrador
      const newDate = new Date(this.currentWeekStartForSecretary);
      newDate.setDate(newDate.getDate() - 7);
      this.loadWeekForSecretary(newDate);
    }
  }

  /**
   * Navega a la semana siguiente
   */
  nextWeek(): void {
    if (this.idDentist) {
      // Vista de dentista
      const newDate = new Date(this.currentWeekStart);
      newDate.setDate(newDate.getDate() + 7);
      this.loadWeek(newDate);
    } else {
      // Vista de secretario/administrador
      const newDate = new Date(this.currentWeekStartForSecretary);
      newDate.setDate(newDate.getDate() + 7);
      this.loadWeekForSecretary(newDate);
    }
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
   * Verifica si un día está disponible (tiene al menos un slot FREE y es hoy o futuro)
   */
  isDayAvailable(day: CalendarDayInterface): boolean {
    // Verificar que el día sea hoy o futuro
    if (!this.isDateTodayOrFuture(day.day)) {
      return false;
    }

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
      selected.startTime === slot.startTime && selected.endTime === slot.endTime
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
   * Verifica si una fecha es hoy o futura (no permite fechas pasadas)
   */
  private isDateTodayOrFuture(date: Date): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Resetear horas para comparar solo la fecha

    const checkDate = new Date(date);
    checkDate.setHours(0, 0, 0, 0);

    return checkDate >= today;
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

    let dentistPerson: PersonInterface;

    // Determinar de dónde obtener los datos del dentista
    const selectedDentist = this.selectedDentist();

    if (selectedDentist) {
      // Vista de secretario/administrador: usar el dentista seleccionado
      const dentistDto = selectedDentist.person;
      dentistPerson = {
        id: dentistDto.id,
        firstName: dentistDto.firstName,
        lastName: dentistDto.lastName,
        dniType: { id: 0, dni: dentistDto.dniType },
        dni: dentistDto.dni,
        birthDate: dentistDto.birthDate,
        gender: {
          id: 0,
          alias: dentistDto.gender,
          name: dentistDto.gender,
        },
        nationality: { id: 0, name: dentistDto.nationality },
        contactEmails: dentistDto.contactEmails.join(", "),
        phoneType: {
          id: 0,
          name: dentistDto.contactPhone[0]?.typePhone || "",
        },
        phone: dentistDto.contactPhone[0]?.phone || "",
        country: {
          id: dentistDto.address.countryId,
          name: dentistDto.address.country,
        },
        province: {
          id: dentistDto.address.provinceId,
          name: dentistDto.address.province,
        },
        locality: {
          id: dentistDto.address.localityId,
          name: dentistDto.address.locality,
        },
        street: dentistDto.address.street,
        number: dentistDto.address.number,
        floor: dentistDto.address.floor,
        apartment: dentistDto.address.apartment,
      };
    } else {
      // Vista de dentista: usar los datos del usuario logueado
      const userData = this.authService.getUserData();
      if (!userData || !userData.person) {
        console.error("No se pudo obtener la información del usuario");
        return;
      }

      dentistPerson = {
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
        nationality: { id: 0, name: userData.person.nationality },
        contactEmails: userData.person.contactEmails.join(", "),
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
    }

    // Construir el objeto AppointmentInterface
    const dateTime = this.buildDateTime(
      this.selectedDay()!.day,
      this.selectedSlot()!.startTime
    );

    const appointment: AppointmentInterface = {
      patient: this.selectedPatient()!,
      dentist: dentistPerson,
      dateTime: dateTime,
      requestSource: selectedDentist
        ? RequestSourceEnum.SECRETARY
        : RequestSourceEnum.DENTIST,
    };

    // Crear el appointment
    this.isSaving.set(true);
    this.appointmentService.create(appointment).subscribe({
      next: (response) => {
        this.isSaving.set(false);
        this.dialogRef.close({
          success: true,
          data: response.data,
        });
      },
      error: (error) => {
        this.isSaving.set(false);
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

  // ===== MÉTODOS PARA VISTA DE SECRETARIO/ADMINISTRADOR =====

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
      specialty = specialty.replace(/^["']|["']$/g, "").trim();

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
   * Maneja la selección de una especialidad
   */
  onSpecialtySelected(specialty: string): void {
    this.selectedSpecialty.set(specialty);
    this.selectedDay.set(null);
    this.selectedSlot.set(null);
    this.selectedDentist.set(null);
    this.availableDentistsForDay.set([]);

    // Obtener dentistas de la especialidad seleccionada
    const group = this.specialtyGroups().find(
      (g) => g.specialtyName === specialty
    );

    if (group) {
      // Cargar las semanas de todos los dentistas de esta especialidad
      this.loadDentistWeeks(group.dentists);
    }
  }

  /**
   * Carga las semanas de todos los dentistas de una especialidad
   * Busca automáticamente la primera semana con disponibilidad
   */
  private loadDentistWeeks(dentists: DentistDtoInterface[]): void {
    this.isLoadingDentistWeeks.set(true);

    // Comenzar desde la semana actual
    this.currentWeekStartForSecretary = new Date();
    this.findFirstAvailableWeek(dentists, new Date(), 0);
  }

  /**
   * Busca recursivamente la primera semana con disponibilidad
   * @param dentists Lista de dentistas a verificar
   * @param startDate Fecha de inicio de la semana a verificar
   * @param weeksChecked Contador de semanas verificadas (para evitar bucles infinitos)
   */
  private findFirstAvailableWeek(
    dentists: DentistDtoInterface[],
    startDate: Date,
    weeksChecked: number
  ): void {
    // Límite de 12 semanas (3 meses) para evitar búsquedas infinitas
    if (weeksChecked >= 12) {
      // Si no se encuentra disponibilidad en 12 semanas, mostrar la semana actual
      this.loadWeekForSecretary(new Date());
      return;
    }

    // Crear un array de observables para cargar todas las semanas en paralelo
    const weekRequests = dentists.map((dentist) =>
      this.calendarService.getWeek(dentist.person.id, startDate).pipe(
        map((response) => ({
          dentist,
          weekData: response.data || null,
        }))
      )
    );

    // Ejecutar todas las peticiones en paralelo
    forkJoin(weekRequests).subscribe({
      next: (availabilities: DentistAvailability[]) => {
        // Verificar si esta semana tiene disponibilidad
        const hasAvailability = this.checkWeekHasAvailability(availabilities);

        if (hasAvailability) {
          // Encontramos una semana con disponibilidad
          this.currentWeekStartForSecretary = startDate;
          this.dentistAvailabilities.set(availabilities);
          this.buildCombinedWeekDays(availabilities);
          this.updateMonthYearLabel(startDate);
          this.isLoadingDentistWeeks.set(false);
        } else {
          // No hay disponibilidad, buscar en la siguiente semana
          const nextWeek = new Date(startDate);
          nextWeek.setDate(nextWeek.getDate() + 7);
          this.findFirstAvailableWeek(dentists, nextWeek, weeksChecked + 1);
        }
      },
      error: (error) => {
        console.error("Error al cargar semanas de dentistas:", error);
        this.isLoadingDentistWeeks.set(false);
      },
    });
  }

  /**
   * Verifica si una semana tiene al menos un slot disponible
   */
  private checkWeekHasAvailability(
    availabilities: DentistAvailability[]
  ): boolean {
    return availabilities.some((availability) => {
      if (!availability.weekData) return false;

      return availability.weekData.days.some((day) => {
        // Verificar que el día sea hoy o futuro
        if (!this.isDateTodayOrFuture(day.day)) return false;

        // Verificar que tenga al menos un slot FREE
        return day.slots.some((slot) => slot.status === SlotStatusEnum.FREE);
      });
    });
  }

  /**
   * Carga una semana específica para la vista de secretario
   */
  private loadWeekForSecretary(date: Date): void {
    const selectedSpecialty = this.selectedSpecialty();
    if (!selectedSpecialty) return;

    const group = this.specialtyGroups().find(
      (g) => g.specialtyName === selectedSpecialty
    );

    if (!group) return;

    this.isLoadingDentistWeeks.set(true);
    this.currentWeekStartForSecretary = date;
    this.updateMonthYearLabel(date);

    // Crear un array de observables para cargar todas las semanas en paralelo
    const weekRequests = group.dentists.map((dentist) =>
      this.calendarService.getWeek(dentist.person.id, date).pipe(
        map((response) => ({
          dentist,
          weekData: response.data || null,
        }))
      )
    );

    // Ejecutar todas las peticiones en paralelo
    forkJoin(weekRequests).subscribe({
      next: (availabilities: DentistAvailability[]) => {
        this.dentistAvailabilities.set(availabilities);
        this.buildCombinedWeekDays(availabilities);
        this.isLoadingDentistWeeks.set(false);
      },
      error: (error) => {
        console.error("Error al cargar semanas de dentistas:", error);
        this.isLoadingDentistWeeks.set(false);
      },
    });
  }

  /**
   * Construye los días de la semana combinando la disponibilidad de todos los dentistas
   */
  private buildCombinedWeekDays(availabilities: DentistAvailability[]): void {
    if (availabilities.length === 0) {
      this.weekDays.set([]);
      return;
    }

    // Usar los días del primer dentista como base
    const firstWeek = availabilities[0].weekData;
    if (!firstWeek || !firstWeek.days) {
      this.weekDays.set([]);
      return;
    }

    // Los días ya vienen del backend, solo necesitamos marcarlos
    this.weekDays.set(firstWeek.days);

    // Seleccionar automáticamente el primer día disponible
    this.selectFirstAvailableDayForSecretary();
  }

  /**
   * Verifica si un día tiene al menos un dentista con slots FREE y es hoy o futuro
   */
  isDayAvailableForSecretary(day: CalendarDayInterface): boolean {
    // Verificar que el día sea hoy o futuro
    if (!this.isDateTodayOrFuture(day.day)) {
      return false;
    }

    const availabilities = this.dentistAvailabilities();

    // Buscar si algún dentista tiene slots FREE en este día
    return availabilities.some((availability) => {
      if (!availability.weekData) return false;

      const dentistDay = availability.weekData.days.find((d) =>
        this.isSameDay(d.day, day.day)
      );

      if (!dentistDay) return false;

      return dentistDay.slots.some(
        (slot) => slot.status === SlotStatusEnum.FREE
      );
    });
  }

  /**
   * Selecciona un día en la vista de secretario
   */
  selectDayForSecretary(day: CalendarDayInterface): void {
    if (!this.isDayAvailableForSecretary(day)) return;

    this.selectedDay.set(day);
    this.selectedSlot.set(null);
    this.selectedDentist.set(null);

    // Filtrar dentistas disponibles para este día
    this.filterAvailableDentistsForDay(day);
  }

  /**
   * Filtra los dentistas que tienen slots FREE en el día seleccionado
   */
  private filterAvailableDentistsForDay(day: CalendarDayInterface): void {
    const availabilities = this.dentistAvailabilities();
    const availableDentists: DentistDtoInterface[] = [];

    availabilities.forEach((availability) => {
      if (!availability.weekData) return;

      const dentistDay = availability.weekData.days.find((d) =>
        this.isSameDay(d.day, day.day)
      );

      if (!dentistDay) return;

      const hasFreeSlots = dentistDay.slots.some(
        (slot) => slot.status === SlotStatusEnum.FREE
      );

      if (hasFreeSlots) {
        availableDentists.push(availability.dentist);
      }
    });

    this.availableDentistsForDay.set(availableDentists);
  }

  /**
   * Maneja la selección de un dentista
   */
  onDentistSelected(dentist: DentistDtoInterface): void {
    this.selectedDentist.set(dentist);
    this.selectedSlot.set(null);

    // Cargar los slots del dentista para el día seleccionado
    const day = this.selectedDay();
    if (day) {
      this.loadSlotsForDentistAndDay(dentist, day);
    }
  }

  /**
   * Carga los slots FREE de un dentista específico para un día específico
   */
  private loadSlotsForDentistAndDay(
    dentist: DentistDtoInterface,
    day: CalendarDayInterface
  ): void {
    const availability = this.dentistAvailabilities().find(
      (a) => a.dentist.person.id === dentist.person.id
    );

    if (!availability || !availability.weekData) {
      this.availableSlots.set([]);
      return;
    }

    const dentistDay = availability.weekData.days.find((d) =>
      this.isSameDay(d.day, day.day)
    );

    if (!dentistDay) {
      this.availableSlots.set([]);
      return;
    }

    const freeSlots = dentistDay.slots.filter(
      (slot) => slot.status === SlotStatusEnum.FREE
    );

    this.availableSlots.set(freeSlots);
  }

  /**
   * Selecciona automáticamente el primer día disponible (para secretario)
   */
  private selectFirstAvailableDayForSecretary(): void {
    const days = this.weekDays();
    const firstAvailableDay = days.find((day) =>
      this.isDayAvailableForSecretary(day)
    );

    if (firstAvailableDay) {
      this.selectDayForSecretary(firstAvailableDay);
    }
  }

  /**
   * Obtiene el nombre completo de un dentista
   */
  getDentistFullName(dentist: DentistDtoInterface): string {
    return `${dentist.person.firstName} ${dentist.person.lastName}`;
  }

  /**
   * Verifica si un dentista está seleccionado
   */
  isSelectedDentist(dentist: DentistDtoInterface): boolean {
    const selected = this.selectedDentist();
    if (!selected) return false;
    return selected.person.id === dentist.person.id;
  }
}
