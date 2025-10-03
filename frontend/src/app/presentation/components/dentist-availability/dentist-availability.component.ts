import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  inject,
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
  DentistAvailabilityInterface,
  DentistDayAvailabilityInterface,
  TimeInterface,
} from "../../../domain/interfaces/dentist.interface";
import { DayEnum } from "../../../utils/enums/day.enum";
import { Subject, takeUntil, debounceTime } from "rxjs";
import { DentistService } from "../../../services/dentist.service";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { SnackbarService } from "../../../services/snackbar.service";

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
export class DentistAvailabilityComponent implements OnChanges, OnDestroy {
  @Input() dentistId: number | null = null;
  @Input() dentistAvailability: DentistDayAvailabilityInterface[] = [];
  @Output() availabilityLoaded = new EventEmitter<void>();
  dentistAvailabilityForm: FormGroup;

  private readonly dentistService = inject(DentistService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly _destroy$ = new Subject<void>();

  private readonly allWeekDays = [
    DayEnum.MONDAY,
    DayEnum.TUESDAY,
    DayEnum.WEDNESDAY,
    DayEnum.THURSDAY,
    DayEnum.FRIDAY,
    DayEnum.SATURDAY,
    DayEnum.SUNDAY,
  ];

  private readonly defaultWorkingDays = [
    DayEnum.MONDAY,
    DayEnum.TUESDAY,
    DayEnum.WEDNESDAY,
    DayEnum.THURSDAY,
    DayEnum.FRIDAY,
  ];

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

  private readonly defaultAppointmentDuration = 30;

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

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["dentistId"] && this.dentistId) {
      this.loadDentistAvailability();
    } else if (changes["dentistAvailability"]) {
      this.initializeForm();
      this.cdr.markForCheck();
    }
  }

  private loadDentistAvailability(): void {
    if (this.dentistId) {
      this.dentistService
        .getDentistAvailability(this.dentistId)
        .pipe(takeUntil(this._destroy$))
        .subscribe({
          next: (
            response: ApiResponseInterface<DentistAvailabilityInterface[]>
          ) => {
            if (response.data && response.data.length > 0) {
              this.dentistAvailability = response.data[0]?.days || [];
            } else {
              this.dentistAvailability = [];
            }
            this.initializeForm();
            this.cdr.markForCheck();
            this.availabilityLoaded.emit();
          },
        });
    } else {
      this.dentistAvailability = [];
      this.initializeForm();
    }
  }

  get availability(): FormArray {
    return this.dentistAvailabilityForm.get("availability") as FormArray;
  }

  private initializeForm(): void {
    while (this.availability.length !== 0) {
      this.availability.removeAt(0);
    }

    if (this.dentistAvailability && this.dentistAvailability.length > 0) {
      this.loadAvailabilityFromBackend();
    } else {
      this.createDefaultAvailability();
    }
  }

  private loadAvailabilityFromBackend(): void {
    const backendDaysMap = new Map<DayEnum, DentistDayAvailabilityInterface>();
    this.dentistAvailability.forEach((dayData) => {
      backendDaysMap.set(dayData.dayName, dayData);
    });

    this.allWeekDays.forEach((day) => {
      const backendData = backendDaysMap.get(day);

      if (backendData) {
        this.availability.push(
          this.createAvailabilityFormGroup(backendData, true)
        );
      } else {
        const nonWorkingDayData: DentistDayAvailabilityInterface = {
          dayName: day,
          startTime: this.nonWorkingTimeValues,
          endTime: this.nonWorkingTimeValues,
          appointmentDuration: 0,
        };
        this.availability.push(
          this.createAvailabilityFormGroup(nonWorkingDayData, false)
        );
      }
    });
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

    const isWorkingControl = formGroup.get("isWorking");
    if (isWorkingControl) {
      isWorkingControl.valueChanges
        .pipe(debounceTime(100), takeUntil(this._destroy$))
        .subscribe((isWorking: boolean | null) => {
          this.toggleFieldsState(formGroup, isWorking ?? false);
          this.cdr.markForCheck();
        });
    }

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
      // Habilitar campos primero
      formGroup.get("startTime.hour")?.enable();
      formGroup.get("startTime.minute")?.enable();
      formGroup.get("endTime.hour")?.enable();
      formGroup.get("endTime.minute")?.enable();
      formGroup.get("appointmentDuration")?.enable();

      // Obtener valores actuales
      const currentStartHour = formGroup.get("startTime.hour")?.value;
      const currentEndHour = formGroup.get("endTime.hour")?.value;
      const currentDuration = formGroup.get("appointmentDuration")?.value;

      // Solo establecer valores por defecto si los campos están en 0 o son null/undefined
      if (
        (currentStartHour === 0 || currentStartHour == null) &&
        (currentEndHour === 0 || currentEndHour == null) &&
        (currentDuration === 0 || currentDuration == null)
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

      // Marcar para verificación de cambios
      this.cdr.markForCheck();
    } else {
      // Establecer valores en 0 y deshabilitar campos
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

      // Marcar para verificación de cambios
      this.cdr.markForCheck();
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  saveAvailability(): void {
    if (!this.dentistId) {
      return;
    }

    const workingDaysData = this.getWorkingDaysData();

    this.dentistService
      .saveDentistAvailability(this.dentistId, workingDaysData)
      .pipe(takeUntil(this._destroy$))
      .subscribe({
        next: (response) => {
          this.snackbarService.openSnackbar(
            response.message || "Disponibilidad guardada exitosamente",
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Success
          );
        },
      });
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
      .map((control, index) => {
        const availability = control.getRawValue();

        return {
          control,
          availability,
          isWorking: availability.isWorking,
        };
      })
      .filter((item) => {
        const isWorking = item.isWorking;
        return isWorking;
      })
      .map((item) => {
        const result = {
          dayName: item.availability.dayName,
          startTime: {
            hour: item.availability.startTime.hour,
            minute: item.availability.startTime.minute,
            second: 0,
            nano: 0 as 0,
          },
          endTime: {
            hour: item.availability.endTime.hour,
            minute: item.availability.endTime.minute,
            second: 0,
            nano: 0 as 0,
          },
          appointmentDuration: item.availability.appointmentDuration,
        };

        return result;
      });
  }

  trackByIndex(index: number): number {
    return index;
  }

  trackByValue(index: number, item: number): number {
    return item;
  }
}
