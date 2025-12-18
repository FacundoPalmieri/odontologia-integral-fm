import { Component, inject, Inject, OnInit, signal } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { CalendarService } from "../../../../services/calendar.service";
import {
  CalendarDayInterface,
  CalendarWeekInterface,
  SlotInterface,
} from "../../../../domain/interfaces/calendar.interface";
import { SlotStatusEnum } from "../../../../utils/enums/appointment/appointment-status.enum";
import { CommonModule } from "@angular/common";

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
    MatDialogModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    IconsModule,
  ],
})
export class CreateAppointmentDialogComponent implements OnInit {
  dialogRef = inject(MatDialogRef<CreateAppointmentDialogComponent>);
  calendarService = inject(CalendarService);

  // Data recibida del diálogo
  idDentist?: number;

  // Estado de carga
  isLoadingWeek = signal(false);
  isLoadingSlots = signal(false);

  // Datos de la semana
  currentWeekStart = new Date();
  weekDays = signal<CalendarDayInterface[]>([]);
  currentMonthYear = "";

  // Día y slot seleccionados
  selectedDay = signal<CalendarDayInterface | null>(null);
  selectedSlot = signal<SlotInterface | null>(null);
  availableSlots = signal<SlotInterface[]>([]);

  // Nombres de días en español
  private dayNames = ["Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab"];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: CreateAppointmentDialogData
  ) {
    this.idDentist = data?.idDentist;
  }

  ngOnInit(): void {
    if (this.idDentist) {
      // Si hay idDentist, cargar la semana actual
      this.loadWeek(new Date());
    }
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
    this.clearSelection();
  }

  /**
   * Navega a la semana siguiente
   */
  nextWeek(): void {
    const newDate = new Date(this.currentWeekStart);
    newDate.setDate(newDate.getDate() + 7);
    this.loadWeek(newDate);
    this.clearSelection();
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
   * Obtiene la etiqueta del día seleccionado
   */
  getSelectedDayLabel(): string {
    const day = this.selectedDay();
    if (!day) return "";

    const date = new Date(day.day);
    const dayName = this.getDayName(date);
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
   * Verifica si se puede guardar (hay día y slot seleccionados)
   */
  canSave(): boolean {
    return this.selectedDay() !== null && this.selectedSlot() !== null;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (!this.canSave()) return;

    const result = {
      day: this.selectedDay(),
      slot: this.selectedSlot(),
    };

    this.dialogRef.close(result);
  }
}
