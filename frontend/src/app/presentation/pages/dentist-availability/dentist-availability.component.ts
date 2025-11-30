import {
  Component,
  effect,
  OnDestroy,
  OnInit,
  inject,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
} from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatInputModule } from "@angular/material/input";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatButtonModule } from "@angular/material/button";
import { MatTabsModule } from "@angular/material/tabs";
import {
  DentistAvailabilityResponseInterface,
  DentistAvailabilitySaveResponseInterface,
  DentistDayAvailabilityInterface,
} from "../../../domain/interfaces/dentist.interface";
import { Subject, takeUntil } from "rxjs";
import { DentistService } from "../../../services/dentist.service";
import { DayEnum, RecurrenceEnum } from "../../../utils/enums/day.enum";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { Router, ActivatedRoute } from "@angular/router";
import { MatCardModule } from "@angular/material/card";
import { MatTooltipModule } from "@angular/material/tooltip";

@Component({
  selector: "app-dentist-availability",
  templateUrl: "./dentist-availability.component.html",
  styleUrl: "./dentist-availability.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatDatepickerModule,
    MatCheckboxModule,
    MatButtonModule,
    MatTabsModule,
    PageToolbarComponent,
    MatCardModule,
    MatTooltipModule,
  ],
})
export class DentistAvailabilityComponent implements OnDestroy, OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dentistService = inject(DentistService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly fb = inject(FormBuilder);

  dentistId: number | null = null;

  // Formulario para disponibilidad semanal recurrente
  weeklyAvailabilityForm: FormGroup = new FormGroup({
    availability: new FormArray([]),
  });

  // Formulario para días específicos
  specificDaysForm: FormGroup = new FormGroup({
    specificDays: new FormArray([]),
  });

  dentistAvailability = signal<DentistAvailabilityResponseInterface | null>(
    null
  );

  // Estado de inicialización
  isConfigured = signal<boolean>(false);
  selectedTabIndex = signal<number>(0);
  isFormValidSignal = signal<boolean>(false);

  private readonly _destroy$ = new Subject<void>();

  private readonly weekDays = [
    DayEnum.MONDAY,
    DayEnum.TUESDAY,
    DayEnum.WEDNESDAY,
    DayEnum.THURSDAY,
    DayEnum.FRIDAY,
    DayEnum.SATURDAY,
    DayEnum.SUNDAY,
  ];

  // Fecha mínima para el datepicker (mañana)
  minDate = new Date(new Date().setDate(new Date().getDate() + 1));

  constructor() {
    // Suscribirse a los cambios de los formularios para actualizar la validez
    this.weeklyAvailabilityForm.valueChanges
      .pipe(takeUntil(this._destroy$))
      .subscribe(() => this._updateFormValidity());

    this.weeklyAvailabilityForm.statusChanges
      .pipe(takeUntil(this._destroy$))
      .subscribe(() => this._updateFormValidity());

    this.specificDaysForm.valueChanges
      .pipe(takeUntil(this._destroy$))
      .subscribe(() => this._updateFormValidity());

    this.specificDaysForm.statusChanges
      .pipe(takeUntil(this._destroy$))
      .subscribe(() => this._updateFormValidity());
  }

  ngOnInit(): void {
    // Obtener el ID del dentista desde la ruta
    const id = this.route.snapshot.paramMap.get("id");
    if (id) {
      this.dentistId = +id; // Convertir string a number
      this._loadAvailability();
    }
  }

  private _loadAvailability(): void {
    if (!this.dentistId) return;

    this.dentistService
      .getAvailability(this.dentistId)
      .pipe(takeUntil(this._destroy$))
      .subscribe({
        next: (response) => {
          this.dentistAvailability.set(response.data);

          // Si hay datos, marcar como configurado y poblar formularios
          if (
            response.data &&
            response.data.days &&
            response.data.days.length > 0
          ) {
            this.isConfigured.set(true);
            this._initializeWeeklyForm();
            this._populateForms();
            // Actualizar validez después de cargar los datos
            this._updateFormValidity();
          }
        },
        error: () => {
          this.dentistAvailability.set(null);
          this.isConfigured.set(false);
        },
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  get weeklyAvailability(): FormArray {
    return this.weeklyAvailabilityForm.get("availability") as FormArray;
  }

  get specificDays(): FormArray {
    return this.specificDaysForm.get("specificDays") as FormArray;
  }

  initializeConfiguration(): void {
    this.isConfigured.set(true);
    this._initializeWeeklyForm();
    // Actualizar validez después de inicializar
    this._updateFormValidity();
  }

  getDayLabel(day: DayEnum): string {
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

  trackByIndex(index: number): number {
    return index;
  }

  trackByValue(index: number, value: number): number {
    return value;
  }

  goBack(): void {
    this.router.navigate(["/profile"]);
  }

  private _updateFormValidity(): void {
    const isValid = this._checkFormValidity();
    this.isFormValidSignal.set(isValid);
  }

  private _checkFormValidity(): boolean {
    // El formulario es válido si al menos uno de los dos tiene datos válidos
    const hasValidWeeklyData =
      this.weeklyAvailabilityForm.valid &&
      this.weeklyAvailability.controls.some(
        (control: AbstractControl) => control.get("isWorking")?.value === true
      );

    const hasValidSpecificDays =
      this.specificDaysForm.valid && this.specificDays.length > 0;

    return hasValidWeeklyData || hasValidSpecificDays;
  }

  isFormValid(): boolean {
    return this.isFormValidSignal();
  }

  addSpecificDay(): void {
    const specificDayGroup = this.fb.group({
      specificDate: [null, Validators.required],
      startTime: this.fb.group({
        hour: [9, Validators.required],
        minute: [0, Validators.required],
      }),
      endTime: this.fb.group({
        hour: [17, Validators.required],
        minute: [0, Validators.required],
      }),
      appointmentDuration: [
        30,
        [Validators.required, Validators.min(5), Validators.max(120)],
      ],
    });

    this.specificDays.push(specificDayGroup);
    // Actualizar la validez después de agregar un día
    this._updateFormValidity();
  }

  removeSpecificDay(index: number): void {
    this.specificDays.removeAt(index);
    // Actualizar la validez después de eliminar un día
    this._updateFormValidity();
  }

  private _initializeWeeklyForm(): void {
    this.weeklyAvailability.clear();

    this.weekDays.forEach((day) => {
      const dayGroup = this.fb.group({
        dayName: [day],
        isWorking: [false],
        startTime: this.fb.group({
          hour: [9, Validators.required],
          minute: [0, Validators.required],
        }),
        endTime: this.fb.group({
          hour: [17, Validators.required],
          minute: [0, Validators.required],
        }),
        appointmentDuration: [
          30,
          [Validators.required, Validators.min(5), Validators.max(120)],
        ],
      });

      dayGroup.get("isWorking")?.valueChanges.subscribe((isWorking) => {
        if (isWorking) {
          dayGroup.get("startTime.hour")?.enable();
          dayGroup.get("startTime.minute")?.enable();
          dayGroup.get("endTime.hour")?.enable();
          dayGroup.get("endTime.minute")?.enable();
          dayGroup.get("appointmentDuration")?.enable();

          dayGroup.get("startTime.hour")?.setValidators([Validators.required]);
          dayGroup
            .get("startTime.minute")
            ?.setValidators([Validators.required]);
          dayGroup.get("endTime.hour")?.setValidators([Validators.required]);
          dayGroup.get("endTime.minute")?.setValidators([Validators.required]);
          dayGroup
            .get("appointmentDuration")
            ?.setValidators([
              Validators.required,
              Validators.min(5),
              Validators.max(120),
            ]);
        } else {
          dayGroup.get("startTime.hour")?.disable();
          dayGroup.get("startTime.minute")?.disable();
          dayGroup.get("endTime.hour")?.disable();
          dayGroup.get("endTime.minute")?.disable();
          dayGroup.get("appointmentDuration")?.disable();

          dayGroup.get("startTime.hour")?.clearValidators();
          dayGroup.get("startTime.minute")?.clearValidators();
          dayGroup.get("endTime.hour")?.clearValidators();
          dayGroup.get("endTime.minute")?.clearValidators();
          dayGroup.get("appointmentDuration")?.clearValidators();
        }

        dayGroup.get("startTime.hour")?.updateValueAndValidity();
        dayGroup.get("startTime.minute")?.updateValueAndValidity();
        dayGroup.get("endTime.hour")?.updateValueAndValidity();
        dayGroup.get("endTime.minute")?.updateValueAndValidity();
        dayGroup.get("appointmentDuration")?.updateValueAndValidity();
      });

      // Deshabilitar todos los campos inicialmente ya que isWorking es false
      dayGroup.get("startTime.hour")?.disable();
      dayGroup.get("startTime.minute")?.disable();
      dayGroup.get("endTime.hour")?.disable();
      dayGroup.get("endTime.minute")?.disable();
      dayGroup.get("appointmentDuration")?.disable();

      this.weeklyAvailability.controls.push(dayGroup);
    });
  }

  private _parseLocalDate(dateString: string | Date | null): Date | null {
    // Si es null, retornar null
    if (dateString === null) {
      return null;
    }

    // Si ya es un objeto Date, retornarlo
    if (dateString instanceof Date) {
      return dateString;
    }

    // Parsear la fecha en formato YYYY-MM-DD como fecha local
    // Esto evita problemas de zona horaria
    const [year, month, day] = dateString.split("-").map(Number);
    return new Date(year, month - 1, day);
  }

  private _populateForms(): void {
    const availabilityData = this.dentistAvailability();

    if (!availabilityData?.days || availabilityData.days.length === 0) {
      return;
    }

    // Separar días semanales de días específicos
    // Los días semanales tienen recurrence, los específicos no
    const weeklyDays = availabilityData.days.filter(
      (d) => d.recurrence === RecurrenceEnum.WEEKLY
    );
    const specificDays = availabilityData.days.filter(
      (d) => !d.recurrence || d.recurrence === RecurrenceEnum.NONE
    );

    // Poblar formulario semanal
    if (weeklyDays.length > 0) {
      this.weeklyAvailability.controls.forEach(
        (dayControl: AbstractControl) => {
          const dayName = dayControl.get("dayName")?.value;
          const dayData = weeklyDays.find((d) => d.dayName === dayName);
          if (dayData) {
            dayControl.patchValue({
              isWorking: true,
              startTime: {
                hour: dayData.startTime.hour,
                minute: dayData.startTime.minute,
              },
              endTime: {
                hour: dayData.endTime.hour,
                minute: dayData.endTime.minute,
              },
              appointmentDuration: dayData.appointmentDuration,
            });
          }
        }
      );
    }

    // Poblar días específicos
    specificDays.forEach((dayData) => {
      const specificDayGroup = this.fb.group({
        specificDate: [
          this._parseLocalDate(dayData.specificDate),
          Validators.required,
        ],
        startTime: this.fb.group({
          hour: [dayData.startTime.hour, Validators.required],
          minute: [dayData.startTime.minute, Validators.required],
        }),
        endTime: this.fb.group({
          hour: [dayData.endTime.hour, Validators.required],
          minute: [dayData.endTime.minute, Validators.required],
        }),
        appointmentDuration: [
          dayData.appointmentDuration,
          [Validators.required, Validators.min(5), Validators.max(120)],
        ],
      });

      this.specificDays.push(specificDayGroup);
    });
  }

  saveAvailability(): void {
    if (!this.weeklyAvailabilityForm.valid && !this.specificDaysForm.valid) {
      console.error("Formularios inválidos");
      return;
    }

    const dentistId = this.dentistId;
    if (!dentistId) {
      console.error("No hay ID de dentista");
      return;
    }

    // Combinar días semanales y específicos
    const allDays: DentistDayAvailabilityInterface[] = [];

    // Agregar días semanales
    const workingDays: DentistDayAvailabilityInterface[] =
      this.weeklyAvailability.controls
        .filter(
          (control: AbstractControl) => control.get("isWorking")?.value === true
        )
        .map((control: AbstractControl) => ({
          dayName: control.get("dayName")?.value,
          recurrence: RecurrenceEnum.WEEKLY,
          specificDate: null, // null para días semanales
          startTime: {
            hour: control.get("startTime.hour")?.value,
            minute: control.get("startTime.minute")?.value,
          },
          endTime: {
            hour: control.get("endTime.hour")?.value,
            minute: control.get("endTime.minute")?.value,
          },
          appointmentDuration: control.get("appointmentDuration")?.value,
        }));

    allDays.push(...workingDays);

    // Agregar días específicos
    const specificDaysData: DentistDayAvailabilityInterface[] =
      this.specificDays.controls.map((control: AbstractControl) => {
        const date = control.get("specificDate")?.value;
        return {
          dayName: null,
          recurrence: null,
          specificDate:
            date instanceof Date ? date.toISOString().split("T")[0] : date,
          startTime: {
            hour: control.get("startTime.hour")?.value,
            minute: control.get("startTime.minute")?.value,
          },
          endTime: {
            hour: control.get("endTime.hour")?.value,
            minute: control.get("endTime.minute")?.value,
          },
          appointmentDuration: control.get("appointmentDuration")?.value,
        };
      });

    allDays.push(...specificDaysData);

    this.dentistService
      .saveAvailability(dentistId, allDays)
      .pipe(takeUntil(this._destroy$))
      .subscribe({
        next: (
          response: ApiResponseInterface<DentistAvailabilitySaveResponseInterface>
        ) => {
          if (response.data.appointmentConflict.length > 0) {
            this.snackbarService.openSnackbar(
              response.message,
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Error
            );
          } else {
            this.snackbarService.openSnackbar(
              response.message,
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success
            );
          }
        },
      });
  }
}
