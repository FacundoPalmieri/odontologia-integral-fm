import {
  Component,
  Input,
  OnInit,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import {
  FormArray,
  FormGroup,
  ReactiveFormsModule,
  FormControl,
  Validators,
} from "@angular/forms";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatInputModule } from "@angular/material/input";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatButtonModule } from "@angular/material/button";
import {
  DentistDayAvailabilityInterface,
  TimeInterface,
} from "../../../domain/interfaces/dentist.interface";
import { DayEnum } from "../../../utils/enums/day.enum";
import { Subject, takeUntil, debounceTime } from "rxjs";

@Component({
  selector: "app-dentist-availability",
  templateUrl: "./dentist-availability.component.html",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  ],
})
export class DentistAvailabilityComponent
  implements OnInit, OnChanges, OnDestroy
{
  @Input() dentistAvailability: DentistDayAvailabilityInterface[] = [];
  dentistAvailabilityForm: FormGroup;

  private readonly _destroy$ = new Subject<void>();

  // Todos los días de la semana
  private readonly allWeekDays = [
    DayEnum.MONDAY,
    DayEnum.TUESDAY,
    DayEnum.WEDNESDAY,
    DayEnum.THURSDAY,
    DayEnum.FRIDAY,
    DayEnum.SATURDAY,
    DayEnum.SUNDAY,
  ];

  // Días laborales por defecto (lunes a viernes activos, sábado y domingo inactivos)
  private readonly defaultWorkingDays = [
    DayEnum.MONDAY,
    DayEnum.TUESDAY,
    DayEnum.WEDNESDAY,
    DayEnum.THURSDAY,
    DayEnum.FRIDAY,
  ];

  // Horarios por defecto
  private readonly defaultStartTime: TimeInterface = {
    hour: 9,
    minute: 0,
    second: 0,
    nano: 0,
  };

  private readonly defaultEndTime: TimeInterface = {
    hour: 17,
    minute: 0,
    second: 0,
    nano: 0,
  };

  private readonly defaultAppointmentDuration = 30; // 30 minutos

  // Valores para días no laborales
  private readonly nonWorkingTimeValues: TimeInterface = {
    hour: 0,
    minute: 0,
    second: 0,
    nano: 0,
  };

  constructor(private cdr: ChangeDetectorRef) {
    this.dentistAvailabilityForm = new FormGroup({
      availability: new FormArray([]),
    });
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["dentistAvailability"]) {
      this.initializeForm();
      this.cdr.markForCheck();
    }
  }

  get availability(): FormArray {
    return this.dentistAvailabilityForm.get("availability") as FormArray;
  }

  private initializeForm(): void {
    // Limpiar el FormArray existente
    while (this.availability.length !== 0) {
      this.availability.removeAt(0);
    }

    if (this.dentistAvailability && this.dentistAvailability.length > 0) {
      // Si hay datos existentes, cargarlos
      this.loadExistingAvailability();
    } else {
      // Si no hay datos, crear valores por defecto
      this.createDefaultAvailability();
    }
  }

  private loadExistingAvailability(): void {
    this.dentistAvailability.forEach((dayAvailability) => {
      const isWorkingDay = this.isWorkingDay(dayAvailability);
      this.availability.push(
        this.createAvailabilityFormGroup(dayAvailability, isWorkingDay)
      );
    });
  }

  private createDefaultAvailability(): void {
    this.allWeekDays.forEach((day) => {
      const isWorkingDay = this.defaultWorkingDays.includes(day);
      const defaultAvailability: DentistDayAvailabilityInterface = {
        dayName: day,
        startTime: isWorkingDay
          ? this.defaultStartTime
          : this.nonWorkingTimeValues,
        endTime: isWorkingDay ? this.defaultEndTime : this.nonWorkingTimeValues,
        appointmentDuration: isWorkingDay ? this.defaultAppointmentDuration : 0,
      };
      this.availability.push(
        this.createAvailabilityFormGroup(defaultAvailability, isWorkingDay)
      );
    });
  }

  private createAvailabilityFormGroup(
    availability: DentistDayAvailabilityInterface,
    isWorkingDay: boolean = true
  ): FormGroup {
    const formGroup = new FormGroup({
      dayName: new FormControl(availability.dayName, [Validators.required]),
      isWorking: new FormControl(isWorkingDay),
      startTime: new FormGroup({
        hour: new FormControl(availability.startTime.hour, [
          Validators.required,
          Validators.min(0),
          Validators.max(23),
        ]),
        minute: new FormControl(availability.startTime.minute, [
          Validators.required,
          Validators.min(0),
          Validators.max(59),
        ]),
      }),
      endTime: new FormGroup({
        hour: new FormControl(availability.endTime.hour, [
          Validators.required,
          Validators.min(0),
          Validators.max(23),
        ]),
        minute: new FormControl(availability.endTime.minute, [
          Validators.required,
          Validators.min(0),
          Validators.max(59),
        ]),
      }),
      appointmentDuration: new FormControl(availability.appointmentDuration, [
        Validators.required,
        Validators.min(5),
        Validators.max(120),
      ]),
    });

    // Configurar listener para el checkbox isWorking con debounce para mejor rendimiento
    const isWorkingControl = formGroup.get("isWorking");
    if (isWorkingControl) {
      isWorkingControl.valueChanges
        .pipe(
          debounceTime(100), // Reducir la frecuencia de actualizaciones
          takeUntil(this._destroy$)
        )
        .subscribe((isWorking: boolean | null) => {
          this.toggleFieldsState(formGroup, isWorking ?? false);
          this.cdr.markForCheck();
        });
    }

    // Configurar estado inicial
    this.toggleFieldsState(formGroup, isWorkingDay);

    return formGroup;
  }

  getDayLabel(day: DayEnum): string {
    const dayLabels = {
      [DayEnum.MONDAY]: "Lunes",
      [DayEnum.TUESDAY]: "Martes",
      [DayEnum.WEDNESDAY]: "Miércoles",
      [DayEnum.THURSDAY]: "Jueves",
      [DayEnum.FRIDAY]: "Viernes",
      [DayEnum.SATURDAY]: "Sábado",
      [DayEnum.SUNDAY]: "Domingo",
    };
    return dayLabels[day];
  }

  private isWorkingDay(availability: DentistDayAvailabilityInterface): boolean {
    // Un día es considerado laboral si tiene horarios válidos (no todos en 0)
    return (
      availability.startTime.hour > 0 ||
      availability.startTime.minute > 0 ||
      availability.endTime.hour > 0 ||
      availability.endTime.minute > 0 ||
      availability.appointmentDuration > 0
    );
  }

  private toggleFieldsState(formGroup: FormGroup, isWorking: boolean): void {
    if (isWorking) {
      // Si marca como día laboral, habilitar campos y establecer valores por defecto
      formGroup.get("startTime.hour")?.enable();
      formGroup.get("startTime.minute")?.enable();
      formGroup.get("endTime.hour")?.enable();
      formGroup.get("endTime.minute")?.enable();
      formGroup.get("appointmentDuration")?.enable();

      // Solo establecer valores por defecto si los campos están en 0
      const currentStartHour = formGroup.get("startTime.hour")?.value;
      const currentEndHour = formGroup.get("endTime.hour")?.value;
      const currentDuration = formGroup.get("appointmentDuration")?.value;

      if (
        currentStartHour === 0 &&
        currentEndHour === 0 &&
        currentDuration === 0
      ) {
        formGroup.patchValue({
          startTime: {
            hour: this.defaultStartTime.hour,
            minute: this.defaultStartTime.minute,
          },
          endTime: {
            hour: this.defaultEndTime.hour,
            minute: this.defaultEndTime.minute,
          },
          appointmentDuration: this.defaultAppointmentDuration,
        });
      }
    } else {
      // Si desmarca, establecer valores en 0 y deshabilitar campos
      formGroup.patchValue({
        startTime: {
          hour: 0,
          minute: 0,
        },
        endTime: {
          hour: 0,
          minute: 0,
        },
        appointmentDuration: 0,
      });

      formGroup.get("startTime.hour")?.disable();
      formGroup.get("startTime.minute")?.disable();
      formGroup.get("endTime.hour")?.disable();
      formGroup.get("endTime.minute")?.disable();
      formGroup.get("appointmentDuration")?.disable();
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  saveAvailability(): void {
    const availabilityData = this.getWorkingDaysData();
    console.log("Días laborables del dentista:", availabilityData);
  }

  getFormData(): DentistDayAvailabilityInterface[] {
    return this.availability.controls.map((control) => {
      const availability = control.getRawValue();
      return {
        dayName: availability.dayName,
        startTime: {
          hour: availability.isWorking ? availability.startTime.hour : 0,
          minute: availability.isWorking ? availability.startTime.minute : 0,
          second: 0,
          nano: 0,
        },
        endTime: {
          hour: availability.isWorking ? availability.endTime.hour : 0,
          minute: availability.isWorking ? availability.endTime.minute : 0,
          second: 0,
          nano: 0,
        },
        appointmentDuration: availability.isWorking
          ? availability.appointmentDuration
          : 0,
      };
    });
  }

  getWorkingDaysData(): DentistDayAvailabilityInterface[] {
    return this.availability.controls
      .map((control) => {
        const availability = control.getRawValue();
        return {
          control,
          availability,
          isWorking: availability.isWorking,
        };
      })
      .filter((item) => item.isWorking) // Solo días marcados como laborables
      .map((item) => ({
        dayName: item.availability.dayName,
        startTime: {
          hour: item.availability.startTime.hour,
          minute: item.availability.startTime.minute,
          second: 0,
          nano: 0,
        },
        endTime: {
          hour: item.availability.endTime.hour,
          minute: item.availability.endTime.minute,
          second: 0,
          nano: 0,
        },
        appointmentDuration: item.availability.appointmentDuration,
      }));
  }

  // TrackBy functions para optimizar el rendimiento de los loops
  trackByIndex(index: number): number {
    return index;
  }

  trackByValue(index: number, item: number): number {
    return item;
  }
}
